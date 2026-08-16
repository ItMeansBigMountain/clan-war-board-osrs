# CWA research and Clan War Board telemetry design

## Product direction

Clan Wars Arena (CWA) is the primary competitive lane. Wilderness (shown as **Wildy**) is a separate secondary lane. Each clan profile and player profile must preserve separate CWA and Wildy records/ratings; results never cross-contaminate ladders.

Leaders arrange a fight by selecting mode, opponent, time, world, location, duration, combat range, matched/full-out options, arena rules and style restrictions. Both leaders must accept the same canonical terms hash. Any change requires reconfirmation.

## Verified CWA mechanics and community terminology

- Clan Wars is a safe PvP minigame with configurable victory and arena rules. “Last team standing” is the closest game-enforced form of a no-return/knockout fight: no one may join or rejoin after the battle starts, and the final surviving team wins.[1]
- **Piling** is multiple members attacking the same opponent. **Transitioning** is changing the pile target quickly. **Tanking** is surviving a coordinated pile through movement, prayer, eating and defensive switches. Binds/freezes prevent movement unless the fight terms disable freezing.[1][2]
- Historical clan-war terminology distinguishes knockout/no-return fights from run-ins where players return until a time or kill cap. The service must therefore lock `mode` and `returnsAllowed` in the accepted terms rather than infer rules from free text.[1][2]
- Two public Clan Wars communities are indexed: Clan Wars Community (Discord server ID `355782774717939713`) and a separate Clan Wars/W2 Clan Wars community (server ID `341701402676166656`). Public indexing verifies that communities exist, but does not expose a trustworthy bot protocol or ranking formula that we should copy.[3][4]
- Wise Old Man and TempleOSRS can enrich registered rosters/profiles, but no public evidence found in this research shows either service recording authoritative CWA kills, tank performance or CWA Elo. Clan War Board must derive fight telemetry from consenting RuneLite clients and keep server confidence explicit.[5][6]

## What makes a strong CWA player

### Offensive execution

- Damage dealt to opponents, separated from friendly fire.
- Pile participation: time attacking the active team target, transition latency, and off-pile attacks.
- Attack cadence and style distribution inferred from animation/projectile/weapon context.
- Bind/freeze attempts and likely lands, with an explicit confidence level.
- Post-fight off-prayer accuracy only when it can be inferred from a visible target overhead at attack time. Never expose a live enemy-prayer aggregation or recommendation.
- Kill involvement is a candidate/assist signal, not definitive credit, unless corroborated by multiple clients.

### Tanking and survival

- Damage received before death or fight end.
- Survival ticks while targeted and number of simultaneous attackers.
- Tiles traveled, direction changes, distance created and successful pile drops.
- Visible defensive overhead uptime and switches, reported only after the fight and with policy-safe privacy.
- Food/prayer/gear actions can be inferred from local-client state/animations, but opponents’ exact inventory, food remaining, potion doses, prayer points and special energy are not observable facts.

### Team and fight quality

- Starting options, unique observed participants, peak active members and survivor curve.
- Clan roster membership at the accepted fight window.
- Registered members of either accepted clan are participants; observed names not on either roster are retained as **non-clan/outsider participants**, never silently assigned to a side.
- Coverage confidence depends on the number of consenting clients, overlapping observations, roster coverage and event agreement.

## RuneLite APIs inspected

Current RuneLite source and API documentation support these observation points:[7][8]

- `GameTick`: canonical tick clock; sample local location, loaded participant positions, distances and fight phase.
- `InteractingChanged#getSource/getTarget` and `Actor#getInteracting`: target changes, pile participation and transition latency.
- `HitsplatApplied#getActor/getHitsplat`, `Hitsplat#getAmount/isMine`: recipient and amount. A hitsplat does not always identify the attacker, so attribution must remain correlated/inferred.
- `AnimationChanged#getActor` plus `Actor#getAnimation`: attack/cast/eat-like attempts after maintaining tested animation classifications.
- `ProjectileMoved#getProjectile/getPosition`; projectile IDs/source/target: ranged/magic correlation, not automatic proof of a hit.
- `ActorDeath#getActor`: observed death. Kill ownership remains inferred from recent damage and cooperating POVs.
- `Player#getOverheadIcon`: visible overhead icon. Jagex/RuneLite policy means no live enemy-prayer aggregation, prayer recommendations, weakness callouts or scouting dashboards.[9][10]
- `Actor#getWorldLocation/getLocalLocation/getWorldView`, `Client#getTopLevelWorldView`, `WorldView#players`: loaded-scene positions only.
- `Client#getClanSettings`, `ClanSettings#getMembers/findMember/titleForRank`, `Client#getClanChannel`, `ClanChannel#getMembers/findMember`, `ClanMember#getName/getRank`: primary roster/rank snapshots. These are client-observed and not cryptographic identity proof.
- `ClanMemberJoined`, `ClanMemberLeft`, `ClanChannelChanged`: roster/channel refresh triggers.

## Replay design

The feasible replay is a **post-fight reconstructed minimap/log replay**, not video:

1. Every consenting client submits bounded event batches with fight ID, terms hash, installation identity, clan, player privacy choice, tick, timestamp, world, region and world point.
2. The service deduplicates events and aligns POVs by tick/time.
3. Stored frames contain observed participant points, target edges, damage/death markers and confidence.
4. The website renders a scrubber, survivor curve, pile-target timeline, event log and minimap-like position view.
5. Missing actors/frames are shown as unknown; the system never interpolates them as fact.

RuneLite only exposes actors in the loaded scene. A single client cannot capture an authoritative full-arena replay. Multi-client corroboration improves coverage but does not make a modified public client perfectly trustworthy.

## Ranking policy

- Separate CWA and Wildy ratings by format and size/rules bucket.
- CWA: result first, then evidence/confidence, damage pressure, tank efficiency, pile participation, transitions, binds and survival. Returns are always zero/disabled.
- Wildy: result, kills/deaths, returns, duration/location control, damage pressure and third-party adjustment.
- Only completed, mutually accepted, non-disputed fights above coverage/confidence thresholds affect public ratings.
- Individual ratings require enough fights and must show sample size/confidence. Team success must outweigh raw damage farming.
- Start with transparent Elo/TrueSkill-style updates; version every formula and preserve the input summary used for each rating change.

## Privacy and Plugin Hub boundary

Core board registration/sync is disclosed. Combat telemetry remains consent-gated. Public player names remain opt-in. Exact future rally details stay private until both leaders accept. Enemy prayer/scouting analysis is never live. Post-fight player detail should be delayed, consented and confidence-labelled. All public Java-client submissions remain untrusted and require rate limits, dedupe and multi-client anomaly checks.

## Sources

1. OSRS Wiki — Clan Wars: https://oldschool.runescape.wiki/w/Clan_Wars
2. Wilderness Guardians — War terminology and tactics: https://www.wildernessguardians.com/w/War
3. Clan Wars Community listing: https://disboard.org/server/355782774717939713
4. Clan Wars/W2 Clan Wars listing: https://discordservers.com/server/341701402676166656
5. Wise Old Man API/docs: https://wiseoldman.net
6. TempleOSRS API documentation: https://templeosrs.com/api_doc.php
7. RuneLite API event package: https://static.runelite.net/runelite-api/apidocs/net/runelite/api/events/package-summary.html
8. RuneLite source inspected at commit `bcdbe627f771e8a5de8cd26bda1c0531f7238fa4`: https://github.com/runelite/runelite/tree/bcdbe627f771e8a5de8cd26bda1c0531f7238fa4
9. RuneLite Plugin Hub information: https://github.com/runelite/runelite/wiki/Information-about-the-Plugin-Hub
10. Jagex third-party client guidelines: https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1
