# Clan War Board

Clan War Board is a RuneLite external plugin for OSRS clans to arrange and analyze Clan Wars Arena (CWA) and Wilderness fights. **CWA is primary** and **Wildy is secondary**. Each format has separate fights, history, clan/player ratings, and performance signals. It is an organization and post-fight analysis board—not a live enemy tracker or scouting tool.

Public website: https://salmon-dune-01c80c60f.7.azurestaticapps.net/

## Panel workflow

The full-width button at the top switches **CWA | Wildy**. CWA defaults to no-return rules and emphasizes damage pressure, tanking, pile participation, transitions, binds and survival. Wildy permits return/location-control metrics. The selected mode is locked into submitted fight terms.

The side panel follows RuneLite's compact group-finder pattern: five fixed-width controls with tooltips, dense fight cards, count badges, explicit empty states, and a persistent connection footer:

1. **C — Clan** — clan overview, installed-plugin coverage, fight counts, next war, and the authenticated player's persisted aggregate war metrics.
2. **B — Board** — switch between count-badged **Open** and **Scheduled** lists. Members see truthful read-only cards. Server-authorized leaders can open an availability post and use it to prefill a new private challenge draft.
3. **+ — Create** — server-authorized leaders can publish a public availability post or send exact private terms to another clan. The form separates common fight fields from private challenge terms.
4. **H — History** — browse completed, service-verified fights and open their details without mixing them into the active board.
5. **↻ — Reload** — reloads the complete clan snapshot, registration/session authorization, coverage, listings, history, and player metrics without changing the current page or Board filter.

The board also performs one guarded background refresh every 60 seconds, so another clan's new or updated post appears without requiring a relog or manual reload. Startup, login, clan-change, post-action, and manual refreshes remain active. Only one board refresh may be in flight at a time. A transient service failure preserves the last good immutable board snapshot and marks it offline instead of showing a false empty board, but only while the player/clan identity generation is unchanged. Refresh responses, cached snapshots, sessions, mutations, completion messages, and telemetry queues are bound to both the player/clan identity and the monotonic login generation that created them. Leader writes are revalidated against live clan membership and rank on RuneLite's client thread immediately before dispatch; telemetry re-reads and binds live identity before every enqueue and flush. Identity changes immediately clear private state, stale and A→B→A responses are rejected, and a refresh for the current identity follows.

Fight details use in-panel back navigation, so returning from a detail page keeps the user in the same tab and list filter. Open details reconcile by stable fight ID after refresh and close cleanly if the service removes the fight. Scheduled fights are selected chronologically rather than trusting response order.

The create form validates ISO-8601 UTC time, duration, combat range, world, and private location before submission. Validation is shown inline without clearing the entered terms, and only one create/challenge request may be active at once.

No fights or clans are fabricated. Empty service collections produce explicit empty states.

## Role handling

RuneLite reads the local player's membership and observed rank from the primary clan. The complete clan roster comes from `ClanSettings`; online channel data is not treated as the complete roster.

Leader controls require both:

- the configured minimum observed clan rank; and
- a current server-issued `leader:write` capability bound to the installation and clan.

Client-side button visibility is not backend authorization. RuneLite-observed rank is useful evidence but is not cryptographic proof from Jagex.

## Configuration

RuneLite settings intentionally contain only:

- `Leader Rank Needed`
- `Share War Telemetry` — optional and disabled by default. When disabled, no combat/location heartbeat or fight telemetry is queued or uploaded.
- `Show My Player Stats Publicly`
- `Show Login Message`

War creation, opponents, dates, worlds, locations, and rules belong inside the panel workflow. The production service URL is pinned in code and is not user-configurable.

## Login message

After the board refresh completes, the plugin displays a high-visibility colored message containing:

- the number of fights needing an opponent; and
- the next scheduled fight, when one exists.

## External API, authentication, and privacy

The plugin talks only to the pinned HTTPS origin `https://salmon-dune-01c80c60f.7.azurestaticapps.net`; it is not user-configurable. Requests identify `ClanWarBoard-RuneLite/1.0` and `X-Clan-War-Board-Client: runelite`.

| Method and route | Purpose / authorization |
| --- | --- |
| `GET /api/health`, `GET /api/clans`, `GET /api/public/availability`, `GET /api/fight-modes` | Public service health, dual-rank clan profiles, board state, and CWA/Wildy schemas. |
| `POST /api/plugin/register` | Sends installation UUID, player/clan names, observed rank, plugin version, and public-stats preference; returns a one-hour bearer session and capabilities. |
| `POST /api/plugin/session/rotate` | `member:read`; revokes/replaces the current session. |
| `GET /api/plugin/me/metrics` | `member:read`; returns owner-only aggregates and recent confirmed-fight events. |
| `POST /api/plugin/availability` | `leader:write`; sends availability terms. |
| `POST /api/plugin/challenges` | `challenge:write`; sends exact proposed terms to another clan. |
| `POST /api/plugin/events/batch` | `telemetry:write`; sends up to 50 confirmed-fight observations. |

Authenticated requests use an opaque bearer session plus a fresh timestamp and UUID nonce. Credentials are never shown in configuration or normal logs. Every member receives `member:read` and may receive `telemetry:write`; the plugin uses telemetry capability only when the separate default-off `Share War Telemetry` setting is enabled. Leader capabilities are server-issued from the observed clan rank, and the plugin rechecks live identity/rank immediately before writes. The trust level is RuneLite client-observed rank, not proof signed by Jagex.

Core board registration is required while logged into a clan and exposes the user's IP address to Azure. Registration sends installation UUID, player/clan names, observed rank, plugin version, and public-stats preference so the service can authorize the clan board. Leader actions send leader-entered fight terms. Optional confirmed-fight event type/amount/world/tick/time/location/evidence/confidence/relation and observed opponent/attacker names are sent only when `Share War Telemetry` is enabled. Turning it off clears buffered events and prevents new telemetry enqueue or upload. When telemetry is enabled but public player stats are disabled, events use private identity handling and appear publicly under stable anonymous labels; owner-only aggregates use one-way installation/player hashes.

When explicitly enabled, telemetry is accepted only for the session clan, confirmed fight, matching world, and scheduled window. The plugin flushes every 10 seconds or 50 events, bounds the queue to 200, and requeues failed batches only while consent remains enabled. Deterministic IDs prevent retry double-counting. Live public schedules omit exact accepted world/location/rules/notes; completed-fight pages publish terms and detailed aggregate/event analysis.

Refresh failures retain an offline-marked immutable snapshot only for the same player, clan, and login generation. Identity changes clear sessions, private/cache state, and telemetry; malformed/non-success responses are treated as API failures.

## Build and test

Use Java 11:

```bash
export JAVA_HOME=/opt/data/jdks/current-java11
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew clean test assemble --no-daemon --console=plain
```

On Windows CMD, use the repository's Java 11 environment and run:

```text
gradlew.bat clean test assemble --no-daemon --console=plain
```

## Manual verification

1. Confirm the settings page contains no service URL, development role, or war form fields; `Share War Telemetry` is off by default.
2. Confirm the panel shows C, B, +, H, and ↻ controls with descriptive tooltips.
3. Confirm Board shows count-badged Open and Scheduled filters.
4. Confirm a member cannot open an unopposed post.
5. Confirm a server-authorized leader can open an unopposed post and prefill a private challenge draft for that clan.
6. Confirm Back returns to the same Board filter.
7. Confirm the Clan tab shows installed/total members as `installed/roster`.
8. Confirm login text is visible and reflects current board data.
9. Confirm empty API collections remain truthful empty states.
10. Confirm History shows only completed service fights and preserves its own back navigation.
11. Confirm ↻ preserves the current page/filter, disables while loading, and refreshes the Clan metrics card.
12. Confirm passive board refresh runs no more than once per 60 seconds and never overlaps another refresh.
13. Confirm a failed refresh keeps the last board cards visible while the footer reports offline/cached state.
14. Confirm changing player or primary clan during a refresh cannot install the previous identity's response.
15. Confirm invalid time, duration, combat range, world, or location stays in the form with an inline error and sends no request.
16. Confirm repeated submission clicks cannot create duplicate in-flight fight requests.
17. Confirm logout, world hop, player change, or primary-clan change immediately removes prior leader controls, player metrics, scheduled/private state, and queued telemetry before the new identity refresh completes.
18. Confirm a failed refresh after changing identity cannot restore the previous identity's cached board or session.
19. Confirm disabled telemetry produces no heartbeat, combat event, queue growth, batch request, or retry; turning it off clears an existing buffer immediately.
20. Confirm the mode button starts on CWA, switches to Wildy without overflow, and sends distinct `mode`/`returnsAllowed` terms.

## CWA research and scoring design

See [`docs/CWA_RESEARCH_AND_TELEMETRY.md`](docs/CWA_RESEARCH_AND_TELEMETRY.md) for sourced CWA mechanics, community findings, player-performance metrics, exact RuneLite observation APIs, roster/outsider validation, ranking policy, privacy limits, and the post-fight minimap/log replay design.
