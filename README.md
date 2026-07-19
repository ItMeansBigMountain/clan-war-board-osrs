# Clan War Board

Clan War Board is a RuneLite external plugin for OSRS clans to publish, accept, schedule, and review consensual clan fights. It is an organization board—not an enemy tracker or scouting tool.

Public website: https://salmon-dune-01c80c60f.7.azurestaticapps.net/

## Panel workflow

The side panel has three fixed navigation tabs and one global reload action:

1. **Clan** — clan overview, installed-plugin coverage, fight counts, next war, and the authenticated player's persisted aggregate war metrics.
2. **Board** — switch between **Needs opponent** and **Scheduled**. Members can read open posts but cannot open or accept them. Server-authorized leaders can open a post and continue to private scheduling.
3. **Private** — server-authorized leaders can publish a public availability post or send exact private terms to another clan.
4. **↻** — reloads the complete clan snapshot, registration/session authorization, coverage, listings, history, and player metrics without changing the current page or Board filter. Existing startup, login, clan-change, and post-action refreshes remain active.

Fight details use in-panel back navigation, so returning from a detail page keeps the user in the same tab and list filter.

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
- `Show My Player Stats Publicly`
- `Show Login Message`

War creation, opponents, dates, worlds, locations, and rules belong inside the panel workflow. The production service URL is pinned in code and is not user-configurable.

## Login message

After the board refresh completes, the plugin displays a high-visibility colored message containing:

- the number of fights needing an opponent; and
- the next scheduled fight, when one exists.

## Third-party networking and privacy

Clan War Board communicates over HTTPS with an Azure service not controlled or verified by the RuneLite developers. Network requests necessarily expose the user's IP address to that service.

Depending on the action and privacy setting, the plugin transmits:

- an opaque random installation UUID;
- the logged-in RuneScape display name;
- primary clan name and client-observed clan rank;
- plugin version;
- availability and challenge terms entered by an authorized leader;
- accepted-fight world, tick, timestamp, region ID, world tile, and plane;
- observed opponent/attacker display names for per-opponent event analysis;
- event type, amount, relationship classification, attribution evidence, and confidence; and
- the logged-in player's public display name only when public player tracking is enabled.

Bearer session credentials are never displayed in configuration or written to normal logs. Exact accepted world, location, and rules are excluded from live public scheduled-fight responses. After the agreed fight window ends, the website publishes the completed fight's terms, cumulative clan/player/opponent analytics, location hotspots, evidence/confidence distributions, and individual event timeline. Players who disable public tracking appear under a stable anonymous label; observed opponent names remain attached to events for verbose analysis.

Telemetry is persisted only when an event matches a confirmed fight's participating clan, world, and scheduled time window. Private aggregates are keyed by a one-way normalized player/clan hash and returned to an authenticated session for that player. Outgoing damage is exact local hitsplat evidence. An observed kill requires recent local damage to the same named player. A return is the first combat observation after the local player's death. Incoming damage amounts are exact, while attacker identity is included only when one nearby player is uniquely interacting with the local player; its evidence and confidence are stored. Non-own-clan actors are labeled `non_own_clan`, not asserted to be members of the agreed opposing clan. Failed batches are requeued and deterministic event IDs prevent retry double-counting.

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

1. Confirm the settings page contains no service URL, development role, or war form fields.
2. Confirm the panel shows Clan, Board, Private, and ↻ controls at the top.
3. Confirm Board shows Needs opponent and Scheduled filters.
4. Confirm a member cannot open an unopposed post.
5. Confirm a server-authorized leader can open an unopposed post and proceed to private setup.
6. Confirm Back returns to the same Board filter.
7. Confirm the Clan tab shows installed/total members as `installed/roster`.
8. Confirm login text is visible and reflects current board data.
9. Confirm empty API collections remain truthful empty states.
10. Confirm ↻ preserves the current page/filter, disables while loading, and refreshes the Clan metrics card.
