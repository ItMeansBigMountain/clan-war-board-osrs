# Production-readiness audit — 2026-08-23

Scope: RuneLite plugin, `clan-war-board-service`, deployed Azure API, and rendered website. OSRS/RuneLite sources only.

## Verified

- Core board sync is required and pinned to the production HTTPS service; there is no sync-disable or endpoint override.
- `Share War Telemetry` is separate, defaults off, gates subscribers/heartbeats/queue/drain/worker/requeue, and clears buffered attribution on opt-out.
- Public player visibility is separate and defaults private.
- Leader UI and dispatch require both live RuneLite clan rank and a current identity-generation-bound server `leader:write` capability.
- Player/clan state, sessions, cached board state, mutations, telemetry, session rotation, and completion UI use context plus a monotonic generation; A→B→A regression coverage exists.
- Real zero-state exists in API, website counters/copy, plugin collections, and tests. Live production preserves the one real plugin-registered clan (`Rs Venom`) and truthfully has no availability posts, completed battles, or CWA/Wildy rating records.
- CWA is primary; CWA and Wildy terms, records, and ratings are separate.
- Every completed-fight website detail includes the replay canvas and honest no-events/no-position states.
- Java 11 `clean test assemble` passed. All 42 service unit tests and both Playwright replay tests passed against the reconciled source. GitHub Actions run [32666470277](https://github.com/ItMeansBigMountain/clan-war-board-service/actions/runs/32666470277) deployed service commit `8f1faf4` successfully through Azure OIDC on 2026-08-23.

## Live Azure deployment evidence — 2026-08-23

- Canonical HTTPS origin: `https://salmon-dune-01c80c60f.7.azurestaticapps.net`.
- `/api/health` returned HTTP 200 with `ok: true`, `storage: cosmos`, and `productionReadyStorage: true` after deployment.
- `/api/clans` and `/api/clans/rs-venom` preserved the existing real plugin registration and exposed only the opted-in public member; no installation UUID/hash, session token, authority claim, or private roster field was returned.
- CWA and Wildy leaderboard routes returned separate unrated standings; both `rating.v1` audit routes returned empty records.
- Public availability and battle routes returned empty arrays with truthful no-real-fights copy. No production clan, challenge, fight, telemetry event, or rating record was seeded for verification.
- The fight-mode contract returned CWA as primary/no-returns and Wildy as secondary/returns-allowed. A nonexistent replay summary returned HTTP 404, proving the service does not fabricate replay data.
- Invalid registration returned HTTP 400 and an unauthenticated owner-metrics request returned HTTP 401.
- `/`, `/clans`, `/fights`, `/leaderboard`, and `/results` each returned the rendered SPA over HTTPS with direct-route HTTP 200 responses and the deployed zero-state UI.
- The release was additive and required no destructive production data rewrite: the existing Cosmos documents remained readable while new roster, moderation, result-confirmation, and rating-audit fields are created only by their authenticated workflows.

## Fixed in this audit slice

1. **High — public visibility could be forged per telemetry event.** The service trusted `event.playerPublic` instead of the registration/session preference. A client could publish a name despite a private registration. Sessions now bind `publicStats`; rotation preserves it; telemetry publication is derived only from the session.
2. **High — observed opponent names were exposed without consent.** Completed-fight public analytics returned raw opponent names. Public events/aggregates now use stable hashed private-opponent labels.
3. **Medium — outsider state was asserted in copy but absent from event output.** Non-own-clan observations now carry the honest `outsider_or_unverified` classification rather than being assigned to either accepted clan.
4. **Medium — replay summaries omitted the fight mode contract.** Public completed-fight summaries now include canonical `mode` and `returnsAllowed`, preserving CWA/Wildy interpretation during replay.

## Fixed in the attacker-minded security pass

1. **High — a single clan could unilaterally submit a winner and trigger rating changes.** Completion now records a canonical result proposal and requires the other participating clan to submit an identical result before the fight becomes completed or affects Elo. Mismatched and same-clan confirmations are rejected.
2. **High — privacy and authority survived re-registration.** A previously public or privileged session remained valid after the same installation re-registered with private settings or changed identity evidence. Registration now revokes every prior session for that installation in memory and Cosmos before issuing the replacement.
3. **High — the public rating audit leaked private accepted-roster hashes, exact fight terms, and arbitrary raw result metadata.** The public route now uses an explicit allowlist containing only rating provenance, the terms hash, bounded result fields, before/after ratings, deltas, and algorithm metadata. Full inputs remain persisted for internal audit and reversals.
4. **Verified boundaries — replay proofs, nonce replay rejection, clock skew, per-session write throttling, immutable acceptance rosters, participant-only challenge access, server-granted leader/moderator capabilities, and generation-bound A→B→A client state all retain automated coverage.** Registration remains the residual unauthenticated abuse surface and should receive an edge/IP limiter at deployment because in-process limits are not reliable across Azure workers.

## Remaining release blockers

1. **Closed in the authority/roster slice — server leader authority no longer trusts client rank alone.** `/api/plugin/register` now issues leader writes only to server-verified leader installations; submitted `clanRank` remains local/client evidence but is not sufficient for `leader:write` or `challenge:write`.
2. **Closed in the authority/roster slice — accepted-roster snapshots are immutable at confirmation.** The plugin submits the private RuneLite primary-clan roster during registration; the service snapshots both clans' hashed rosters at mutual acceptance and uses those snapshots for rival-vs-outsider classification.
3. **Closed in the network/lifecycle coverage slice — plugin network and async regressions now have Java coverage.** MockWebServer tests exercise reordered board JSON, malformed/empty API responses, generation-safe delayed A→B→A behavior, privacy-specific registration and telemetry request bodies, telemetry opt-out discard behavior after drain, and executor rejection cleanup.
4. **Closed in the browser replay coverage slice — replay UI now has automated browser coverage.** Playwright tests exercise completed-fight replay play, pause, scrub, no-position event rendering, and mode-specific completed-fight terms using routed fixture API responses.
5. **Closed in the dual-rating slice — verified results now drive separate versioned ratings.** `rating.v1` applies Elo independently per CWA/Wildy mode only after mutual acceptance, immutable roster snapshots, sufficient telemetry confidence, and a non-disputed result. Every applied update persists its exact inputs, algorithm, before/after ratings, and deltas; the public audit route exposes those records.
6. **Closed in the moderation/leader-operations slice — result disputes and safe moderator decisions are auditable.** Participants can dispute completed results with bounded outsider/crasher/telemetry evidence, immediately reversing any applied rating. Only server-verified moderator installations receive `moderation:write`; correction and void decisions require a reason, persist an append-only audit record, and expose a sanitized member read-only history. Terminal challenge states reject stale accept/counter/cancel actions. The RuneLite panel now keeps accept, cancel, and dispute forms in the panel rather than config.

## Remaining release work

1. **Closed — deploy and probe the reconciled service commit.** Azure OIDC run 32666470277 deployed `8f1faf4`; live Cosmos health, privacy-filtered clan data, separate CWA/Wildy zero-state ratings, empty fight collections, replay-not-found behavior, invalid-write rejection, and all direct website routes were verified over HTTPS.
2. **Closed — independent security and abuse review.** The attacker-minded pass fixed unilateral rating completion, stale privacy/authority sessions, and public rating-audit disclosure. Remaining deployment hardening is an Azure edge/IP limit on unauthenticated registration; authenticated writes already enforce nonce, clock-skew, optimistic-concurrency, and per-session limits.
3. **Closed — leader/moderator operations.** Challenge lifecycle enforcement, participant disputes, evidence review, correction/void flows, rating reversal, moderation audit history, scoped capabilities, and member read-only views are implemented with service and plugin coverage.

## Release decision

Deployment, security, moderation, and visual-QA gates are now closed. Keep this repository in `in-progress` until the queued isolated end-to-end release validation and Plugin Hub compliance/package review pass. The open Who's Grinding submission also blocks opening another Plugin Hub PR by the user-approved queue rule.
