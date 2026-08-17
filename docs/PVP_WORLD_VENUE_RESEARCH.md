## Findings

### World 392 and the PvP rotation

- **World 392 is a permanent Australian members PvP world.** Jagex originally reassigned it from Seasonal Deadman to PvP, and the current OSRS world documentation lists it under **Always Active**, not either weekly rotation.[5][9]
- The current rotation model uses one-week periods:
  - **Always active:** 308/316 Wilderness PK F2P, 365 High Risk, 369 Wilderness PK members, **392 PvP**, 474 High Risk, 533 High Risk.
  - **Rotation A:** 318 BH, 390 LMS, **539 PvP**, **548 high-risk PvP**, 559 LMS, 569 BH, **577 F2P PvP**.
  - **Rotation B:** 319 BH, **560 PvP**, **561 F2P PvP**, **579 high-risk PvP**, 580 LMS.[9]
- Using the Wiki’s published rotation epoch, **Rotation A is active on 17 August 2026**, with the next change approximately 20 August. This should be treated as informational only; rotation delays and temporary all-world availability have happened before.[1][9]
- Consequently, **do not hardcode either rotation or even 392’s status** in validation logic. It is reasonable to show 392 prominently because it is normally permanent, but its live `PVP` flag must remain authoritative.

### RuneLite world data and correct validation

RuneLite exposes:

- `Client.getWorldList()` for the live world list and `Client.getWorld()` for the current world.[8]
- Each `World` supplies `getId()`, `getTypes()`, `getActivity()`, membership/type information, location, address and player count.[7]
- Current `WorldType` values include `MEMBERS`, `PVP`, `BOUNTY`, `PVP_ARENA`, `SKILL_TOTAL`, `HIGH_RISK`, `LAST_MAN_STANDING`, `BETA_WORLD`, `NOSAVE_MODE`, `TOURNAMENT_WORLD`, `FRESH_START_WORLD`, `DEADMAN`, and `SEASONAL`, plus legacy flags.[6]

Recommended validation:

```java
World selected = Arrays.stream(client.getWorldList())
    .filter(w -> w.getId() == selectedWorld)
    .findFirst()
    .orElse(null);

boolean availablePvPWorld =
    selected != null
    && selected.getTypes().contains(WorldType.PVP);
```

Important details:

1. **Require `WorldType.PVP` explicitly.** Do not use `WorldType.isPvpWorld()` for this feature without another check: RuneLite’s implementation intentionally returns true for both `PVP` and `DEADMAN`. A normal PvP-war selector should not silently accept Deadman.[6]
2. Presence in `getWorldList()` establishes that the world is presently available; `PVP` establishes its current role.
3. Treat `HIGH_RISK` as a separate, prominent warning/term. High-risk PvP worlds lose all carried/worn items and disable Protect Item.[1][9]
4. Check `MEMBERS` against the proposed venue and participating clans.
5. Revalidate:
   - when the selector opens,
   - immediately before publishing/accepting fight terms,
   - when displaying an existing fight after a weekly update,
   - and after login/world hop.
6. Once logged in, verify the actual current-world type with `client.getWorldType().contains(PVP)` rather than trusting saved terms alone.
7. If the world list is temporarily unavailable, show **“world status unavailable—retry”**, not “not a PvP world.”
8. Save the world number in fight terms, but derive labels such as PvP/high-risk/F2P/member dynamically. A previously valid rotating world should become **temporarily unavailable**, not corrupt or silently switch to another number.

### PvP-world mechanics relevant to wars

- Outside safe zones, PvP-world combat has a base **±15 combat-level range**.[1]
- Inside the Wilderness on a PvP world, the effective attack range is **15 plus the local Wilderness level**. For example, level 20 Wilderness acts as a ±35 bracket, although death mechanics still use the actual level 20 Wilderness value.[4]
- Safe zones include the majority of banks, respawn points, the Grand Exchange, Ferox Enclave, and specifically protected locations. Combat cannot be initiated there, although an existing retaliated fight may continue briefly after entry.[1]
- Documented additional safe locations include:
  - Ape Atoll bank
  - Barbarian Outpost
  - Blast Mine bank chests
  - Emir’s Arena/Duel Arena entrance
  - Fossil Island North Island bank
  - Fossil Island Museum Camp bank-chest tent
  - Soul Wars lobby
  - Kourend Castle building, all floors
  - Volcanic Mine bank
  - Xeric’s Glade grape vines.[1]
- Boats cannot be attacked; respawn banks and armour stands are safe.[1]
- The current documented PvP-world PJ timer is 16 ticks/9.6 seconds. Single-way fights lock the two participants against interruption.[1]
- Food/potions cannot be picked up for 15 ticks after PvP combat, and trading is blocked for 20 ticks outside multicombat areas.[1]
- Theatre of Blood is inaccessible on PvP worlds.[1]

### Multicombat locations documented by the Wiki

The OSRS Wiki describes this as **“Some multicombat areas include”**, so it is a documented list, not a guaranteed exhaustive geometry specification. Multi is shown in-game by the crossed-swords icon; outside multi is normally single-way, except singles-plus locations.[3]

#### Ordinary/non-Wilderness areas

- Majority of oceans, except parts of the Ardent Ocean
- Abyss
- Al Kharid Palace
- Ape Atoll
- Kharidian Desert Bandit Camp
- Barbarian Assault
- Barbarian Village
- Battlefield south of West Ardougne
- Castle Wars
- Draynor Village jail area
- Emir’s Arena
- Most of Falador, including White Knights’ Castle
- Mole Hole
- TzHaar Fight Pit
- God Wars Dungeon
- Hosidius cow pen
- Jatizso ice-troll area
- Jormungand’s Prison
- Kalphite Lair
- Western Kharazi Jungle
- Lighthouse Dungeon
- Neitiznot ice-troll area
- Pest Control
- Piscatoris Fishing Colony
- Player-owned-house dungeons and fight arenas
- Ranging Guild
- Northern Fremennik Province coast
- Stronghold of Security first level
- Mor Ul Rek
- Varrock Sewers
- Waterbirth Island Dungeon
- White Wolf Mountain
- Inner Kraken Cove
- Wizards’ Tower
- Woodcutting Guild.[3]

#### Wilderness areas

The documented Wilderness subset includes areas at or around:

- Abandoned Farm/eastern Ruins
- Wilderness Bandit Camp
- Western Wilderness Chaos Temple/church
- Dark Warriors’ Fortress
- Deadly red spiders
- Deep Wilderness Castle Ruins
- Demonic Ruins
- Graveyard of Shadows
- Lava Maze
- Lava Dragon Isle
- Rogues’ Castle
- Scorpion Pit
- Southern Wilderness Agility Course.[3]

The Wiki’s broader PvP-hotspot documentation also identifies current multi venues such as the deep multi-boss caves, Wilderness Slayer Cave, eastern Chaos Temple/zombie pirates, Fountain of Rune and Wilderness God Wars Dungeon.[2] These may be newer or more granular than the older summary list, reinforcing that exact venue geometry should come from maintained tile data rather than names alone.

#### Locations not suitable as dangerous PvP-world war venues

Do **not** turn the Wiki list directly into a selectable war list:

- Barbarian Assault, Castle Wars, Fight Pits, Pest Control, Emir’s Arena and POH fight arenas are minigame/private combat contexts rather than ordinary dangerous open-world PvP venues.
- Any bank or explicitly protected sub-area remains safe even if embedded in a generally multicombat region—for example, Ape Atoll’s bank.
- Theatre of Blood cannot be entered on PvP worlds.[1]
- Ancient Guthixian Temple is documented only as a special NPC-focused pseudo-multi exception: the game considers it single-way, so it is unsuitable.
- Ocean/boat areas are unsuitable because players on boats cannot be attacked.[1][3]
- Singles-plus areas such as Revenant Caves, the current Wilderness Agility Course combat area, and lesser Wilderness bosses are not multi merely because several player/NPC interactions are possible.[3]

Thus, a PvP-world venue must be in the intersection:

```text
documented/verified multicombat tiles
∩ dangerous open-world PvP tiles
∩ reachable on the selected world/account type
− safe-zone tiles
− minigame/private/quest-only exceptions
```

### Venue selection: PvP world versus Wilderness

Use separate venue modes and separate validation rules.

#### Wilderness venue

- PvP works on **every ordinary world**; “Wilderness PK” is only an activity label and adds no special mechanics.[2][9]
- Validate the exact tile/area against Wilderness multi geometry.
- Show actual Wilderness level and its corresponding combat bracket.
- Existing Wilderness teleport restrictions, safe enclaves, underground planes, singles-plus zones and death rules remain relevant.
- On a PvP world, add the extra 15 levels to the displayed attack bracket.[4]

#### PvP-world venue outside the Wilderness

- Requires a currently available world whose types explicitly contain `PVP`.
- World selection is a required part of the fight terms.
- Only offer maintained, exact-tile non-Wilderness multi venues that are outside safe zones.
- Display a fixed base ±15 combat bracket.
- Surface nearby safe-zone boundaries because a bank, respawn point or protected building can cut through or border an otherwise usable area.
- Distinguish standard PvP from high-risk PvP and F2P from members.

#### Recommended product model

```text
VenueMode:
  WILDERNESS
  PVP_WORLD

Venue:
  id
  displayName
  polygon/rectangle union
  plane
  mode
  membersRequired
  dangerousPvPVerified
  multiVerified
  safeZoneExclusions
  sourceRevision
```

Do not reuse Wilderness-only polygons for non-Wilderness PvP venues. Multi boundaries, safe zones, access rules and planes are independent dimensions. Validate selected spawn/meeting tiles and the intended fighting footprint, not merely a named city or map region.

No repository files were modified.

## Sources

[1] [OSRS Wiki: PvP world](https://oldschool.runescape.wiki/w/PvP_world)  
[2] [OSRS Wiki: Player killing](https://oldschool.runescape.wiki/w/Player_killing)  
[3] [OSRS Wiki: Multicombat area](https://oldschool.runescape.wiki/w/Multicombat_area)  
[4] [OSRS Wiki: Wilderness](https://oldschool.runescape.wiki/w/Wilderness)  
[5] [Jagex: Improvements to Blast Furnace, Blast Mine, and Lighthouse Dungeon](https://secure.runescape.com/m=news/a=97/improvements-to-blast-furnace-blast-mine-and-lighthouse-dungeon?oldschool=1)  
[6] [RuneLite API: WorldType](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/WorldType.html)  
[7] [RuneLite API: World](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/World.html)  
[8] [RuneLite source: Client API](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Client.java)  
[9] [OSRS Wiki: World list and PvP rotation](https://oldschool.runescape.wiki/w/World)