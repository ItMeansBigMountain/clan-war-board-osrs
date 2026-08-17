# OSRS Clan War Board: timed TDM and KOTH rules research

## Bottom line

Use **team deaths**, not named killer credit, as the authoritative scoring primitive. A RuneLite client can reliably observe a visible actor's death, but `ActorDeath` identifies only the dead actor; `HitsplatApplied` identifies the damaged actor and hitsplat, not the attacker. Projectile source/target and interaction state can improve confidence but do not solve melee, AoE, poison/venom, delayed damage, simultaneous attackers, actors outside scene visibility, or clients that are absent/offline. Therefore, individual “kills” should be labelled **estimated/assisted attribution**, while team score is the corroborated count of eligible opponent deaths.[1][2][3][4]

Recommended production presets:

- **Timed TDM:** 20 minutes, unlimited returns, optional 60-second return delay, highest corroborated eligible-death score; optional mercy cap at 50 (small fights) or 100 (large fights).
- **KOTH:** 20 minutes, 3 public pre-approved multi-only hill polygons, rotate every 5 minutes, sample each opted-in participant once per game tick but aggregate server-side into **5-second scoring frames**; uncontested majority earns 1 point/frame, ties earn 0; first to 180 points or high score at time.
- Run only in a mutually accepted **world + UTC window + bounded location polygon**. Freeze a canonical terms hash before start.
- Wilderness and PvP-world leaderboards should be separate buckets; risk, respawn, combat-range and interruption conditions are materially different.

## What OSRS already establishes

Clan Wars itself provides strong precedents: First-to-X offers 25 through 10,000 kills and permits entry during the battle; Most Kills offers 5/10/20/60/120-minute matches; Oddskull uses 100/300/500 point caps. Rejoining can be unrestricted or delayed 60 seconds. Team caps include 5v5 through 30v30. The rules also expose leaving-channel penalties and single-spell/PJ options.[5]

The clan community distinguishes knockout wars from PKRIs/CWRIs where dead players re-gear and return. Community war terms commonly specify boundaries and gear restrictions; outcomes include clear, outlast, most remaining at a time cap, or a kill cap. Crashing is a recognized integrity problem and may lead to pause, restart, or draw.[6] Historical Clan Wars documentation also reports diminishing credit for repeatedly killing the same enemy, a useful anti-farming precedent, though this is an older RuneScape guide rather than proof of current OSRS implementation.[7]

## Timed TDM rules

### Match terms

Lock before acceptance:

- world and venue polygon;
- start time, 2-minute ready window, duration (default 20m; allow 10/20/30/60);
- roster policy: fixed roster or named substitutes; combat bracket; team-size cap;
- returns: unlimited, 60-second delay, or no-return knockout;
- gear/spell/prayer/food rules (informational in Wilderness/PvP worlds—the plugin cannot enforce them);
- score cap/mercy rule;
- crash threshold and pause/void procedure;
- telemetry/privacy disclosure and leaderboard bucket.

### Scoring

1. +1 team point for an **eligible opponent death** in the accepted world, time and location window.
2. Require victim-side death telemetry when possible; corroborate with one or more independent participant clients. Never require a uniquely named killer.
3. Mark a death `confirmed`, `probable`, or `uncorroborated`. Only confirmed/probable count live; only confirmed count ranked unless both leaders ratify the result.
4. A death outside the boundary, after logout/hop, before the start, or after expiry does not count.
5. Suicide/environment/third-party ambiguity: count the death only if recent opposing-clan damage is corroborated; otherwise flag for review. Do not invent a killer.
6. Returns are inferred when the same rostered player is again heartbeating inside the combat polygon after a death and any agreed delay. Respawn itself is not a score.

### Caps and anti-farming

- **Time cap is primary.** An optional mercy cap ends early: 25 for <=10v10, 50 for <=20v20, 100 above that. These align with existing Clan Wars first-to-kill options.[5]
- Repeated-victim controls: first 3 deaths in a rolling 10-minute window score normally; later deaths score only if the victim had spent at least 60 seconds alive/in bounds and had dealt or received meaningful opponent damage. Flag extreme victim/team concentration rather than silently changing score.
- No points during the agreed return cooldown, safe-zone staging, or grace period after re-entry (recommend 10 seconds unless the returner attacks first).
- Detect impossible event rates, duplicate deterministic event IDs, one-client-only streaks, roster churn, world hops, and outsider damage. Require bilateral coverage for ranked results.
- If third-party damage/deaths exceed an agreed threshold (recommend 10% of deaths or 3 consecutive affected deaths), auto-pause scoring and ask both leaders to resume, relocate, or void.

## KOTH rules

### Hill definition and selection

- Maintain a reviewed catalogue of **multi-only polygons**, each with region/plane, centre, radius/polygon, approach routes, exclusions (banks, doors, ladders, safe tiles), and a live `MULTICOMBAT_AREA` validation requirement.
- At confirmation, the server commits to `SHA-256(matchId || termsHash || orderedCandidateIds || nonce)`. Reveal the nonce and deterministic shuffled sequence when the ready window opens. This prevents either leader or server operator from choosing a favorable hill after seeing attendance.
- Use 3 distinct hills, no immediate repeat, minimum separation, and symmetric/roughly comparable access. If a chosen polygon fails live multi validation for multiple clients, skip to the next committed candidate.
- The hill is a match rule/objective, not a tactical scanner. Show its fixed boundary and timer to both teams; do not highlight opponents, recommend tiles, or expose opponent counts/prayers.

### Control sampling

- Clients submit only their **own** `(tick, world point, inMulti, alive)` heartbeat. The server maps points into the public hill polygon.
- Aggregate into 5-second frames (roughly 8 game ticks). A player counts only with >=50% heartbeat coverage in that frame, in bounds, alive, rostered, and past return grace.
- Majority mode: team A controls when `A >= 1` and `A > B`; team B likewise; equal or empty is neutral. Award 1 point per frame. This avoids high-frequency jitter and makes partial packet loss tolerable.
- Optional stricter preset: require a 2-player minimum for fights >=10v10 and 2 consecutive winning frames before control flips. Do not award based on damage or kills while on hill; that encourages farming rather than control.
- Rotate every 5 minutes with a 15-second neutral transition announcement. Do not move the hill reactively based on live players.
- First to 180 points ends early; otherwise high score at 20 minutes wins. At 12 frames/minute, the theoretical 20-minute maximum is 240.

### KOTH anti-abuse

- Cap each installation/account at one occupancy vote; dedupe by account/session/roster identity.
- Never count observer-supplied locations for other players. RuneLite explicitly rejects crowdsourcing player locations/gear/names.[9]
- Require balanced telemetry coverage; if one team falls below 60% expected roster coverage for 30 seconds, mark frames low-confidence and stop ranked accrual until recovered.
- Exclude safe zones and transition paths. A death does not itself change hill points.

## Tie-breakers

Apply in this fixed order and publish it in terms:

**TDM:** (1) higher confirmed deaths, excluding merely probable; (2) fewer own eligible deaths (relevant if some opponent deaths were unconfirmed); (3) higher unique-victim count; (4) fewer outsider-affected deaths; (5) 5-minute sudden-death extension; (6) draw if telemetry remains insufficient.

**KOTH:** (1) more controlled frames in final hill; (2) longest uninterrupted control streak; (3) more distinct rostered players who validly occupied a hill; (4) 3-minute sudden-death on the next committed hill, first uncontested 3 consecutive frames; (5) draw.

Avoid damage-dealt as a primary tie-breaker: RuneLite does not expose complete, attacker-labelled global combat telemetry, and different tanking/gear styles distort it.[1][2][3]

## Venues

### Wilderness

Only catalogue polygons that are wholly multi. Multi means a player can be attacked by more than one player/monster, and AoE attacks can hit multiple opponents there.[8] Validate `MULTICOMBAT_AREA` on every local-player heartbeat and reject boundary-crossing samples. Community sources confirm Wilderness wars, returning, boundaries, crashes and multi tactics such as piles/clumps.[6]

Operationally: keep the exact world/location private until both leaders accept; publicize only after completion. Supply loss, combat-level Wilderness range, escape routes and crashes make this the high-risk bucket.

### PvP worlds outside Wilderness

PvP worlds permit combat almost everywhere except safe zones; their attackable range is equivalent to level 15 Wilderness. They add respawn-area bank chests and rotate availability. Falador is the strongest established outside-Wilderness multi venue: the Wiki calls it the most common PvP-world multicombat location, and most of the city is multi.[10][11]

Candidate catalogue:

1. **Falador city/park sectors** — preferred; broad multi, banks/safe zones nearby, teleport access. Carefully exclude both banks and respawn/safe tiles. Falador respawn requires Recruitment Drive, so do not assume equal death-return access.[10][12]
2. **Barbarian Village** — the entire village is multi, but aggressive NPCs create environmental attribution noise; use only with explicit NPC-interference rules.[13]
3. **Varrock Sewers** — large multi dungeon, but monsters, chokepoints, web/tool access and underground escape routes make it a niche preset, not default.[14]

Do not assume Lumbridge, Camelot, Grand Exchange, Rimmington or Castle Wars lobby are multi merely because they are PvP hotspots. The Wiki classifies common Lumbridge/GE combat as 1v1, while Camelot and other member hotspots are not documented there as multi.[10] Every catalogue polygon needs in-client varbit testing before production.

PvP-world returns should use agreed rally/teleport routes, not death-respawn equality: players can configure different respawn points and some require unlocks. Recommended rule is **return by any legal route after 60 seconds**, with scoring re-enabled only after an in-bounds heartbeat; do not require a particular respawn.

## RuneLite telemetry limits and safe UX

- `HitsplatApplied` fires for a hitsplat processed on an actor even if not rendered, but supplies only actor + hitsplat. `Hitsplat` has amount/type and `isMine`/`isOthers`; it does not provide a universal attacker identity.[1][2]
- `ActorDeath` supplies only the actor that died.[3]
- `Actor.getInteracting()` is interaction state, not proof of damage; interactions include trade/follow and can be null outside visibility. Actor health is transmitted as a ratio, not true HP.[4]
- Projectiles can expose nullable source/target actors, but AoE can have no actor target and this does not cover melee or all delayed effects.[15]
- Consequently, collect local-player events and self-location only, merge multiple POVs server-side, and display confidence. Do not promise authoritative individual KC.

Jagex prohibits opponent-clan indicators, scouting information, group summaries such as attackable outsiders/prayers, opponent freeze timers, opponent-target indicators, and PvP Attack/cast menu manipulation.[9] RuneLite also states it will not accept plugins that crowdsource other players' locations, gear or names, or expose player information over HTTP.[16]

Safe interface:

- side panel: accepted terms, match clock, aggregate team score/control bar, confidence/coverage, crash/pause state;
- world overlay: only the pre-agreed hill boundary and neutral rotation timer, equal for both teams;
- post-match: delayed aggregate replay/heatmap from consenting players' own data, private-by-default identities;
- never highlight enemies/allies as tactical targets, show opponent positions/counts/prayers/freeze timers, suggest where to stand, modify PvP menus, or upload observed third-party player locations.

## Sources

1. RuneLite `HitsplatApplied`: https://static.runelite.net/runelite-api/apidocs/net/runelite/api/events/HitsplatApplied.html
2. RuneLite `Hitsplat`: https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Hitsplat.html
3. RuneLite `ActorDeath`: https://static.runelite.net/runelite-api/apidocs/net/runelite/api/events/ActorDeath.html
4. RuneLite `Actor`: https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Actor.html
5. OSRS Wiki, Clan Wars: https://oldschool.runescape.wiki/w/Clan_Wars
6. Wilderness Guardians, War: https://www.wildernessguardians.com/w/War
7. Tip.It, Clan Wars (historical RuneScape guide): https://www.tip.it/runescape/pages/view/clan_wars.htm
8. OSRS Wiki, Multicombat area: https://oldschool.runescape.wiki/w/Multicombat_area
9. Jagex, Third Party Client Guidelines: https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1
10. OSRS Wiki, PvP world: https://oldschool.runescape.wiki/w/PvP_world
11. OSRS Wiki, Free-to-play PvP culture: https://oldschool.runescape.wiki/w/Free-to-play_PvP_culture
12. OSRS Wiki, Spawning: https://oldschool.runescape.wiki/w/Spawning
13. OSRS Wiki, Barbarian Village: https://oldschool.runescape.wiki/w/Barbarian_Village
14. OSRS Wiki, Varrock Sewers: https://oldschool.runescape.wiki/w/Varrock_Sewers
15. RuneLite `Projectile`: https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Projectile.html
16. RuneLite, Rejected or Rolled Back Features: https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features
