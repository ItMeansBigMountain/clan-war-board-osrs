# Production-readiness audit — 2026-08-23

Scope: RuneLite plugin, `clan-war-board-service`, deployed Azure API, and rendered website. OSRS/RuneLite sources only.

## Verified

- Core board sync is required and pinned to the production HTTPS service; there is no sync-disable or endpoint override.
- `Share War Telemetry` is separate, defaults off, gates subscribers/heartbeats/queue/drain/worker/requeue, and clears buffered attribution on opt-out.
- Public player visibility is separate and defaults private.
- Leader UI and dispatch require both live RuneLite clan rank and a current identity-generation-bound server `leader:write` capability.
- Player/clan state, sessions, cached board state, mutations, telemetry, session rotation, and completion UI use context plus a monotonic generation; A→B→A regression coverage exists.
- Real zero-state exists in API, website counters/copy, plugin collections, and tests. Live production currently has one real plugin-registered clan and no rated fights.
- CWA is primary; CWA and Wildy terms, records, and ratings are separate.
- Every completed-fight website detail includes the replay canvas and honest no-events/no-position states.
- Java 11 `clean test assemble` passed. All 35 service unit tests and both Playwright replay tests passed against the reconciled source. The earlier live health, clans, leaderboard, replay, zero-state, and dual-mode probes predate these unpublished commits and must be repeated after deployment.

## Fixed in this audit slice

1. **High — public visibility could be forged per telemetry event.** The service trusted `event.playerPublic` instead of the registration/session preference. A client could publish a name despite a private registration. Sessions now bind `publicStats`; rotation preserves it; telemetry publication is derived only from the session.
2. **High — observed opponent names were exposed without consent.** Completed-fight public analytics returned raw opponent names. Public events/aggregates now use stable hashed private-opponent labels.
3. **Medium — outsider state was asserted in copy but absent from event output.** Non-own-clan observations now carry the honest `outsider_or_unverified` classification rather than being assigned to either accepted clan.
4. **Medium — replay summaries omitted the fight mode contract.** Public completed-fight summaries now include canonical `mode` and `returnsAllowed`, preserving CWA/Wildy interpretation during replay.

## Remaining release blockers

1. **Closed in the authority/roster slice — server leader authority no longer trusts client rank alone.** `/api/plugin/register` now issues leader writes only to server-verified leader installations; submitted `clanRank` remains local/client evidence but is not sufficient for `leader:write` or `challenge:write`.
2. **Closed in the authority/roster slice — accepted-roster snapshots are immutable at confirmation.** The plugin submits the private RuneLite primary-clan roster during registration; the service snapshots both clans' hashed rosters at mutual acceptance and uses those snapshots for rival-vs-outsider classification.
3. **Closed in the network/lifecycle coverage slice — plugin network and async regressions now have Java coverage.** MockWebServer tests exercise reordered board JSON, malformed/empty API responses, generation-safe delayed A→B→A behavior, privacy-specific registration and telemetry request bodies, telemetry opt-out discard behavior after drain, and executor rejection cleanup.
4. **Closed in the browser replay coverage slice — replay UI now has automated browser coverage.** Playwright tests exercise completed-fight replay play, pause, scrub, no-position event rendering, and mode-specific completed-fight terms using routed fixture API responses.
5. **Closed in the dual-rating slice — verified results now drive separate versioned ratings.** `rating.v1` applies Elo independently per CWA/Wildy mode only after mutual acceptance, immutable roster snapshots, sufficient telemetry confidence, and a non-disputed result. Every applied update persists its exact inputs, algorithm, before/after ratings, and deltas; the public audit route exposes those records.
6. **Closed in the moderation/leader-operations slice — result disputes and safe moderator decisions are auditable.** Participants can dispute completed results with bounded outsider/crasher/telemetry evidence, immediately reversing any applied rating. Only server-verified moderator installations receive `moderation:write`; correction and void decisions require a reason, persist an append-only audit record, and expose a sanitized member read-only history. Terminal challenge states reject stale accept/counter/cancel actions. The RuneLite panel now keeps accept, cancel, and dispute forms in the panel rather than config.

## Remaining release work

1. **Deploy and probe the reconciled service commit.** The local source and suites are green, but the authority, roster, rating, and replay changes must be deployed before live behavior can be claimed.
2. **Complete independent security and abuse review.** Server-issued authority, session persistence/rotation, roster snapshots, rating completion, telemetry consent, replay sanitization, and rate limits still require the queued red-team pass.
3. **Closed — leader/moderator operations.** Challenge lifecycle enforcement, participant disputes, evidence review, correction/void flows, rating reversal, moderation audit history, scoped capabilities, and member read-only views are implemented with service and plugin coverage.

## Release decision

Not ready for a Plugin Hub PR. Keep this repository in `in-progress` until deployment probes and the queued security/moderation work pass. The open Who's Grinding submission also blocks opening another Plugin Hub PR by the user-approved queue rule.
