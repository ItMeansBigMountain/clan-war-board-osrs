# Production-readiness audit — 2026-08-23

## Plugin Hub policy gate

- Re-checked the current RuneLite Plugin Hub README, RuneLite Rejected/Rolled-Back Features page (updated 2026-06-29), and Jagex Third Party Client Guidelines.
- Removed the plugin's complete combat/location telemetry lane, opponent-name observation, clan-roster upload, public nearby-player profile fetch, and player overhead rendering. RuneLite explicitly rejects plugins that crowdsource other players' names, locations, gear, or similar data; Jagex also prohibits PvP scouting and opposing-clan indicators.
- Production configuration now exposes only the minimum clan-rank preference and login-message preference. The HTTPS service origin remains pinned and has no user-configurable setting.
- Registration sends only the installation UUID, local player name, primary clan name, observed local rank, and plugin version. No roster, nearby-player, opponent, combat, gear, or location observations are collected or uploaded.
- `runelite-plugin.properties` now includes `version=1.0.0` and `build=standard`; the Plugin Hub standard build therefore uses only RuneLite's bundled dependency set.
- The root and toolbar icons are matching 48x48 RGBA product assets. Their SHA-256 is `ff8645ea44ab2f8de003da32aaf267e9055ae430f32b4a4e58b378031421d081`; the only identical files in the portfolio scan were this repository's root/resource/build copies.

Scope: RuneLite plugin, `clan-war-board-service`, deployed Azure API, and rendered website. OSRS/RuneLite sources only.

## Verified

- Core board sync is required and pinned to the production HTTPS service; there is no sync-disable or endpoint override.
- The production plugin has no combat/location telemetry lane, roster upload, nearby-player profile, opponent observation, player-visibility control, or overhead rendering.
- Leader UI and dispatch require both live RuneLite clan rank and a current identity-generation-bound server `leader:write` capability.
- Player/clan state, sessions, cached board state, mutations, session rotation, and completion UI use context plus a monotonic generation; A→B→A regression coverage exists.
- Real zero-state exists in API, website counters/copy, plugin collections, and tests. Live production preserves the one real plugin-registered clan (`Rs Venom`) and truthfully has no availability posts, completed battles, or CWA/Wildy rating records.
- CWA is primary; CWA and Wildy terms, records, and ratings are separate.
- Completed-fight website details are limited to mutually confirmed clan-level results; no replay, combat-event, opponent, or location observations are presented.
- Java 11 `clean test assemble` passed. All 42 service unit tests passed against the reconciled source. GitHub Actions run [32666470277](https://github.com/ItMeansBigMountain/clan-war-board-service/actions/runs/32666470277) deployed service commit `8f1faf4` successfully through Azure OIDC on 2026-08-23.

## Live Azure deployment evidence — 2026-08-23

- Canonical HTTPS origin: `https://salmon-dune-01c80c60f.7.azurestaticapps.net`.
- `/api/health` returned HTTP 200 with `ok: true`, `storage: cosmos`, and `productionReadyStorage: true` after deployment.
- `/api/clans` and `/api/clans/rs-venom` preserved the existing real plugin registration and exposed only the opted-in public member; no installation UUID/hash, session token, authority claim, or private roster field was returned.
- CWA and Wildy leaderboard routes returned separate unrated standings; both `rating.v1` audit routes returned empty records.
- Public availability and battle routes returned empty arrays with truthful no-real-fights copy. No production clan, challenge, fight, or rating record was seeded for verification.
- The fight-mode contract returned CWA as primary/no-returns and Wildy as secondary/returns-allowed.
- Invalid registration returned HTTP 400 and an unauthenticated owner-metrics request returned HTTP 401.
- `/`, `/clans`, `/fights`, `/leaderboard`, and `/results` each returned the rendered SPA over HTTPS with direct-route HTTP 200 responses and the deployed zero-state UI.
- The release was additive and required no destructive production data rewrite: the existing Cosmos documents remained readable while new roster, moderation, result-confirmation, and rating-audit fields are created only by their authenticated workflows.

## Fixed in this audit slice

1. **High — the original design exceeded Plugin Hub privacy boundaries.** Production no longer collects or uploads combat events, locations, opponent names, rosters, nearby-player profiles, gear, or other-player observations.
2. **High — public result copy implied telemetry-backed proof.** Public completed-fight responses and website details now describe only mutually confirmed clan-level results.
3. **Medium — release UI retained obsolete personal analytics.** The production panel now presents clan identity, coverage, clan-level records, and scheduled fights only.

## Fixed in the attacker-minded security pass

1. **High — a single clan could unilaterally submit a winner and trigger rating changes.** Completion now records a canonical result proposal and requires the other participating clan to submit an identical result before the fight becomes completed or affects Elo. Mismatched and same-clan confirmations are rejected.
2. **High — privacy and authority survived re-registration.** A previously public or privileged session remained valid after the same installation re-registered with private settings or changed identity evidence. Registration now revokes every prior session for that installation in memory and Cosmos before issuing the replacement.
3. **High — the public rating audit leaked private accepted-roster hashes, exact fight terms, and arbitrary raw result metadata.** The public route now uses an explicit allowlist containing only rating provenance, the terms hash, bounded result fields, before/after ratings, deltas, and algorithm metadata. Full inputs remain persisted for internal audit and reversals.
4. **Verified boundaries — nonce replay rejection, clock skew, per-session write throttling, participant-only challenge access, server-granted leader/moderator capabilities, and generation-bound A→B→A client state all retain automated coverage.** Registration remains the residual unauthenticated abuse surface and should receive an edge/IP limiter at deployment because in-process limits are not reliable across Azure workers.

## Remaining release blockers

1. **Closed in the authority/roster slice — server leader authority no longer trusts client rank alone.** `/api/plugin/register` now issues leader writes only to server-verified leader installations; submitted `clanRank` remains local/client evidence but is not sufficient for `leader:write` or `challenge:write`.
2. **Closed in the result-confirmation slice — results require mutual confirmation.** Both participating clans must submit an identical bounded clan-level result before completion or rating updates; no roster or event evidence is uploaded by the production plugin.
3. **Closed in the network/lifecycle coverage slice — plugin network and async regressions have Java coverage.** MockWebServer tests exercise reordered board JSON, malformed/empty API responses, generation-safe delayed A→B→A behavior, registration request privacy, and executor rejection cleanup.
4. **Closed in the dual-rating slice — mutually confirmed results drive separate versioned ratings.** `rating.v1` applies Elo independently per CWA/Wildy mode after mutual result confirmation and persists bounded rating provenance.
5. **Closed in the moderation/leader-operations slice — result disputes and safe moderator decisions are auditable.** Participants can dispute completed results; correction and void decisions require a reason, persist an append-only audit record, and expose a sanitized member read-only history. Terminal challenge states reject stale accept/counter/cancel actions.

## Remaining release work

1. **Closed — deploy and probe the reconciled service commit.** Azure OIDC run 32666470277 deployed `8f1faf4`; live Cosmos health, privacy-filtered clan data, separate CWA/Wildy zero-state ratings, empty fight collections, invalid-write rejection, and all direct website routes were verified over HTTPS.
2. **Closed — independent security and abuse review.** The attacker-minded pass fixed unilateral rating completion, stale privacy/authority sessions, and public rating-audit disclosure. Remaining deployment hardening is an Azure edge/IP limit on unauthenticated registration; authenticated writes already enforce nonce, clock-skew, optimistic-concurrency, and per-session limits.
3. **Closed — leader/moderator operations.** Challenge lifecycle enforcement, participant disputes, evidence review, correction/void flows, rating reversal, moderation audit history, scoped capabilities, and member read-only views are implemented with service and plugin coverage.

## Release decision

Deployment, security, moderation, visual-QA, isolated release validation, and Plugin Hub compliance/package gates are closed. The release candidate is in `pr-review-pending`; the open Who's Grinding submission still blocks opening another Plugin Hub PR under the user-approved queue rule.

## P0 release validation — 2026-08-23T21:24:35Z

- **Java/plugin:** Java 11 `./gradlew clean test assemble --no-daemon --console=plain` passed with Temurin 11.0.31.
- **Service/API:** `python3 -m unittest discover -s tests -v` passed all 42 `clan-war-board-service` tests.
- **Website:** direct-route and source checks confirmed that public result copy is limited to mutually confirmed clan-level results and contains no replay or combat/location analytics claims.
- **Isolated end-to-end workflow:** release validation exercised registration, server leader verification, availability, challenge, counter, mutual result confirmation, dispute, moderation void, separate CWA/Wildy rating updates, and public privacy filtering without writing production data.
- **Live read-only probes:** the canonical Azure origin returned Cosmos production-ready health, one preserved real clan, privacy-filtered clan profile data, separate CWA/Wildy standings, empty `rating.v1` audit records, empty availability/battle collections, the CWA/Wildy fight-mode contract, and HTTP 200 direct SPA routes for `/`, `/clans`, `/fights`, `/leaderboard`, and `/results`.
- **Migration/IaC:** `terraform`/`tofu` are not installed on this host, so full provider validation could not run locally. As a fallback, `uv run --with python-hcl2` parsed all five Terraform files and verified the additive Cosmos shape: free-tier Cosmos account plus `clans`, `wars`, and `summaries` SQL containers with partition keys.
