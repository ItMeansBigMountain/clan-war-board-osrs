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
- Java 11 `clean test assemble` passed. Service unit suite passed. Live health, clans, both leaderboard modes, and website replay/zero-state/dual-mode markup were probed.

## Fixed in this audit slice

1. **High — public visibility could be forged per telemetry event.** The service trusted `event.playerPublic` instead of the registration/session preference. A client could publish a name despite a private registration. Sessions now bind `publicStats`; rotation preserves it; telemetry publication is derived only from the session.
2. **High — observed opponent names were exposed without consent.** Completed-fight public analytics returned raw opponent names. Public events/aggregates now use stable hashed private-opponent labels.
3. **Medium — outsider state was asserted in copy but absent from event output.** Non-own-clan observations now carry the honest `outsider_or_unverified` classification rather than being assigned to either accepted clan.
4. **Medium — replay summaries omitted the fight mode contract.** Public completed-fight summaries now include canonical `mode` and `returnsAllowed`, preserving CWA/Wildy interpretation during replay.

## Remaining release blockers

1. **Critical — server leader authority is still self-asserted.** `/api/plugin/register` is unauthenticated and grants `leader:write` solely from client-submitted `clanRank`. Local live-rank rechecks are good client safety, but the server capability is not an independent authority. Before Plugin Hub submission, add a real clan-claim/leader-verification mechanism or restrict leader writes to a clearly community-trust beta environment.
2. **High — accepted-roster snapshots are not implemented.** The service does not persist immutable rosters for both clans at mutual acceptance, so it cannot distinguish an accepted rival member from an outsider. Current output deliberately says `outsider_or_unverified`; it must not claim definitive outsider attribution until roster snapshots exist.
3. **High — plugin network/lifecycle coverage is incomplete.** Java tests cover pure helpers and state predicates but not the required MockWebServer matrix: malformed/empty/reordered JSON, delayed A→B→A responses, telemetry opt-out after drain, request capture by privacy state, executor rejection, and retry cleanup.
4. **Medium — replay UI lacks automated browser behavior coverage.** Service aggregation tests cover replay data and empty data, but no browser test exercises play/pause, scrub, no-position events, or mode-specific completed-fight rendering.
5. **Medium — dual rankings are schema-only today.** The live API separates CWA/Wildy fields, but both are unrated and no versioned rating-update pipeline applies completed verified results yet.

## Release decision

Not ready for a Plugin Hub PR. Keep this repository in `in-progress`. The open Who's Grinding submission also blocks opening another Plugin Hub PR by the user-approved queue rule.
