# CWA portal and clan-culture research

Date: 2026-10-11

## Decision summary

Clan War Board should model **the purple Challenge portal's real game modes and options**, then support the social negotiation around them. It should not invent its own CWA combat engine.

The current preset concepts need refinement:

- `CWA Knockout` maps cleanly to the game's **Last team standing** mode.
- `CWA First to kills` maps cleanly to **First to X kills**, but the target must be one of the game's values: 25, 50, 100, 200, 500, 1,000, 5,000, or 10,000.
- A timed CWA TDM should use the game's **Most kills** mode and one of its durations: 5, 10, 20, 60, or 120 minutes.
- CWA KOTH should use the game's **Oddskull** mode (100, 300, or 500 points), not a custom location-tracking implementation.
- Wildy TDM/KOTH remain separate community-agreed Wilderness formats. They cannot claim game enforcement and must remain leader-confirmed workflows without opponent/location telemetry.

## Verified in-game CWA flow

Source: OSRS Wiki, Clan Wars
https://oldschool.runescape.wiki/w/Clan_Wars

Source: OSRS Wiki, Challenge portal
https://oldschool.runescape.wiki/w/Challenge_portal

- Clan Wars is a safe minigame at Ferox Enclave.
- Organised fights use the **purple Challenge portal**. The white portal is free-for-all and has no configured clan teams or challenge terms.
- The game is organised around opposing **chat-channels**. A participant ranked Captain or higher in a chat-channel challenges a Captain-or-higher member of another chat-channel.
- The leaders configure and agree to terms in game.
- Members are notified and use the purple portal to enter or observe.
- A two-minute entry/preparation timer runs behind a separating wall; the wall drops when combat begins.
- Death sends players to a waiting/jail area. Re-entry depends on the selected game mode/options.
- Viewing orbs allow spectators to observe.
- Maximum documented size is 100 players per side.

### Game-enforced victory modes

- **Last team standing**: ends when one team is eliminated; no joining or rejoining after combat starts.
- **First to X kills**: 25, 50, 100, 200, 500, 1,000, 5,000, or 10,000; fighters may enter during the battle.
- **Most kills**: 5, 10, 20, 60, or 120 minutes; fighters may enter during the battle.
- **Oddskull**: first to 100, 300, or 500 points.

### Game-enforced options that belong in structured terms

- Arena/map (12 documented maps, with four members-only).
- Melee allowed/disabled.
- Ranged allowed/disabled.
- Magic: all spellbooks, standard, Classic F2P, binding only, or disabled.
- Food allowed/disabled.
- Drinks allowed/disabled.
- Prayer: all, no overheads, or disabled.
- Special attacks: allowed, no Staff of the Dead, or disabled.
- Team cap: uncapped, 5v5, 10v10, 15v15, 20v20, or 30v30.
- Leaving the channel: counts as death or no penalty.
- Rejoining: unrestricted or 60-second delay where applicable.
- Miscellaneous options include ignore freezing, PJ timer, single spells, trident, and straggler thresholds.

The board should store these as selectable values matching the portal, not as one free-text `rules` field. Free text should remain only for community rules the portal cannot enforce.

## Clan culture and coordination

Source: Wilderness Guardians, War
https://www.wildernessguardians.com/w/War

Source: Vengeance public forums and Aftermath board
https://tastevengeance.com/forums/

Source: Vengeance CWA guide (community-specific/historical tactics)
https://tastevengeance.com/forums/topic/18520-clan-wars-guide-for-zerks-tanks-and-more/

Source: RuneScape Community, competitive clanning discussion (historical)
https://runescapecommunity.com/showthread.php?pid=963

- A fight begins socially with a **declaration** or direct staff-to-staff approach.
- Historically declarations and diplomacy lived on clan forums; modern coordination largely moved to Discord.
- The other clan discusses, counters, accepts, or rejects terms.
- Common organisers are leaders, warlords, or other clan staff—not every member.
- Clans announce a **prep**, publish the agreed terms, and expect members to prepare gear/supplies and attendance.
- Members **mass** before the event, normally 30–60 minutes early for major fights.
- Clans compare starting numbers and may **cut** members to produce **matched opts** (for example 10v10 or 20v20).
- Common formats include knockout/no-return, CWRI (Clan Wars run-in/returning), matched opts, miniwars, fun wars, and inners.
- Typical terms extend beyond the portal: rounds, attacking side/first attack, bounds, gear/build/defence caps, sniping rules, member lists, substitutions, and conduct.
- Voice calling and role discipline are culturally important: callers select piles; tanks survive piles; mages bind/freeze; members follow transitions.
- A completed clean fight commonly ends with an **ending** screenshot/count, `good fight` messages, and an **aftermath** forum/social post containing results, screenshots, and often video.
- Rankings historically relied heavily on public aftermath posts, which makes evidence completeness and selective reporting a known weakness.
- Crashing is culturally serious in Wilderness wars; responses include pausing, anti-crash cooperation, relocating/restarting, or declaring a draw. This concern is less central to the game-instanced purple portal.

## Product implications

### What Clan War Board should do

1. **Portal-accurate CWA builder**
   - Choose the exact game mode and valid target/duration/points.
   - Choose arena and every portal-enforced option.
   - Produce a concise in-game setup checklist for the two Captains.

2. **Diplomacy workflow**
   - Declaration/open availability, direct challenge, counter, accept/reconfirm, reject/cancel.
   - Store both clans' staff contacts and immutable accepted terms hash.

3. **Prep workflow**
   - Private member briefing, mass time, portal time, world, chat-channel names, gear/build rules, rounds, attack order, and attendance intent.
   - Keep exact operational details private until accepted.

4. **Matched-opts support**
   - Target team size, starting pull, cut count, substitutes, and final leader-confirmed participant list.
   - Do not crowdsource or publish nearby players.

5. **Round and result workflow**
   - Support best-of-N/round-by-round outcomes around the game's individual matches.
   - Record the game mode, game-reported winner, leader confirmations, ending count, and optional screenshots/video links.
   - Ratings change only after matching confirmations and dispute checks.

6. **Aftermath and history**
   - Generate a clean public recap from confirmed facts: clans, format, rounds, scores, winner, date, approved media, and `good fight` acknowledgement.
   - Preserve disputes/corrections and evidence provenance.

### What it should not do

- Do not automate the portal, issue game challenges, click interfaces, or modify PvP actions.
- Do not infer or expose opponent roles, prayers, piles, positions, freezes, gear, or targets.
- Do not invent a custom CWA KOTH when Oddskull already provides a game-enforced objective.
- Do not call a free-text, leader-scored mode game-enforced.
- Do not equate RuneLite primary-clan rank with chat-channel Captain authority; they are different systems and must be presented as separate evidence.

## Recommended next implementation slice

1. Replace the two generic CWA presets with four portal-native choices: Last team standing, First to X kills, Most kills, and Oddskull.
2. Add dependent target/duration/points selectors with only the values supported by the game.
3. Add structured portal options and arena selection.
4. Add rounds, matched opts, attack order, prep/mass time, and private chat-channel fields.
5. Keep Wildy presets separate and explicitly community-enforced.
6. Add an in-panel `Portal setup checklist` rather than trying to control the game interface.

## Source quality notes

- OSRS Wiki is the primary current mechanics source and includes recent 2024–2026 change history.
- Wilderness Guardians is a living clan-maintained cultural source with current examples, but terminology and customs can vary by clan.
- Vengeance and RuneScape Community pages are valuable evidence of CWA culture, tactics, aftermaths, and competitive norms; some material is historical or clan-specific and should not be treated as universal game mechanics.
