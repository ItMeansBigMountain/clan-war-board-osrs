# Multi-area catalog

Sources: `https://github.com/tsbreuer/Multi-Lines` @ `858246aa7381584c81b1a9a94c67af595a75a51a`; `https://github.com/Nightfirecat/plugin-hub-plugins` @ `d81bb4c444c3772b1563be8a8fcc2252c84a0a4f`. Bounds half-open. Planes are not encoded. Layer=`floor(y/6400)` (not game plane).

## Verified counts
- `{'total_named_areas': 116, 'total_rectangles': 1075, 'area_status': {'active': 95, 'removed': 1, 'disabled': 20}, 'by_source': {'multi-lines': {'named_areas': 84, 'rectangles': 1043, 'active_areas': 63, 'disabled_areas': 20, 'removed_areas': 1}, 'wilderness-lines': {'named_areas': 32, 'rectangles': 32, 'active_areas': 32, 'disabled_areas': 0, 'removed_areas': 0}}, 'exact_duplicate_pairs': 22, 'positive_area_overlap_pairs': 46, 'cross_source_overlap_pairs': 26}`

## Named areas

| ID | Name | enabled | removed | wilderness | rects | layers | categories | notes |
|---|---|---:|---:|---:|---:|---|---|---|
| `multi-lines:a001` | Weiss | True | False | False | 25 | 0 | non-wilderness | - |
| `multi-lines:a002` | Troll Stronghold / GWD | True | False | False | 5 | 0 | non-wilderness | - |
| `multi-lines:a003` | Death Plateau | True | False | False | 4 | 0 | non-wilderness | - |
| `multi-lines:a004` | Buthorpe / White Wolf Mountain | True | False | False | 2 | 0 | non-wilderness | - |
| `multi-lines:a005` | Falador chaos altar | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a006` | Falador | True | False | False | 11 | 0 | non-wilderness | - |
| `multi-lines:a007` | Barbarian village | True | False | False | 4 | 0 | non-wilderness | - |
| `multi-lines:a008` | Draynor Jail | True | False | False | 15 | 0 | non-wilderness | - |
| `multi-lines:a009` | Wizard Tower | True | False | False | 2 | 0 | non-wilderness | - |
| `multi-lines:a010` | Al Kharid | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a011` | Desert Bandit Camp | True | False | False | 16 | 0 | non-wilderness | - |
| `multi-lines:a012` | Mort Myre swamp | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a013` | Burgh de rott | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a014` | South from burgh | False | True | False | 2 | 0 | non-wilderness,dmm | Doesnt seem to be multi on normal worlds, might be on DMM |
| `multi-lines:a015` | South east of port phasmatys | True | False | False | 10 | 0 | non-wilderness | - |
| `multi-lines:a016` | south west of port phasmatys | True | False | False | 14 | 0 | non-wilderness | - |
| `multi-lines:a017` | Past Bridge south of port phasmatys | True | False | False | 8 | 0 | non-wilderness | - |
| `multi-lines:a018` | Top left of fossil island | True | False | False | 2 | 0 | non-wilderness | - |
| `multi-lines:a019` | Bottom left of fossil island | True | False | False | 9 | 0 | non-wilderness | - |
| `multi-lines:a020` | Top main beach of fossil island | True | False | False | 39 | 0 | non-wilderness | Holy fuck jagex what the fuck is this multi zone mapping |
| `multi-lines:a021` | Rellekka | True | False | False | 3 | 0 | non-wilderness | - |
| `multi-lines:a022` | Settlement Ruins Kourend | True | False | False | 60 | 0 | non-wilderness | Pending rework to reduce rectangles used |
| `multi-lines:a023` | Kharazi Jungle | True | False | False | 13 | 0 | non-wilderness | - |
| `multi-lines:a024` | Ape Atoll | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a025` | Pest Control | True | False | False | 1 | 0 | non-wilderness,minigame,dmm | There's no relevant tiles that benefit from multi warning but it may be unexpected - I think its disabled on dmm anyway |
| `multi-lines:a026` | Ogre boat feldip hills to karamja | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a027` | Castle Wars | False | False | False | 1 | 0 | non-wilderness,minigame | There's no relevant tiles outside the minigame that benefit from the multi warning |
| `multi-lines:a028` | Jiggig | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a029` | Necromancer | True | False | False | 2 | 0 | non-wilderness | - |
| `multi-lines:a030` | Gnome Battlefield | True | False | False | 2 | 0 | non-wilderness | - |
| `multi-lines:a031` | Ranging guild | True | False | False | 3 | 0 | non-wilderness | - |
| `multi-lines:a032` | MM2 Area north of gnome stronghold | True | False | False | 2 | 0 | non-wilderness | We map this twice since its a map link and the real tiles are north on the real map |
| `multi-lines:a033` | Arandar | True | False | False | 2 | 0 | non-wilderness | - |
| `multi-lines:a034` | Piscatoris Fishing Colony | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a035` | Fremmenik Isles | True | False | False | 4 | 0 | non-wilderness | - |
| `multi-lines:a036` | AJS Fairy ring penguin island (North of Miscenallia) | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a037` | Pirate's cove | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a038` | Lunar Isle | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a039` | Zulrah Island | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a040` | Poison Waste | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a041` | Soul Wars Minigame | False | False | False | 16 | 0 | non-wilderness,minigame | Not really needed to be displayed since its a safe area and its already implied its multi |
| `multi-lines:a042` | Soul Wars island northern patch | True | False | False | 15 | 0 | non-wilderness,minigame | - |
| `multi-lines:a043` | Kourend Sand crabs | True | False | False | 99 | 0 | non-wilderness | Holy jesus christ this one took a while |
| `multi-lines:a044` | Altar north of sand crabs | True | False | False | 8 | 0 | non-wilderness | - |
| `multi-lines:a045` | Hosidius Mine | True | False | False | 7 | 0 | non-wilderness | Hard to tell real multi tiles due to game obstacles |
| `multi-lines:a046` | Random Hosidius Multi areas like square, kitchen and fields | True | False | False | 26 | 0 | non-wilderness | - |
| `multi-lines:a047` | Piscarilius | True | False | False | 30 | 0 | non-wilderness | - |
| `multi-lines:a048` | Arceus main area | True | False | False | 72 | 0 | non-wilderness | - |
| `multi-lines:a049` | Tower of Magic Arceus | True | False | False | 5 | 0 | non-wilderness | - |
| `multi-lines:a050` | Lovakengj | True | False | False | 12 | 0 | non-wilderness | - |
| `multi-lines:a051` | Kourend Castle | True | False | False | 17 | 0 | non-wilderness | - |
| `multi-lines:a052` | One weird hosidius house | True | False | False | 1 | 0 | non-wilderness | - |
| `multi-lines:a053` | Forthos dungeon, saltpetre mine & woodcutting guild | True | False | False | 53 | 0 | non-wilderness | - |
| `multi-lines:a054` | Barbarian Camp near land's end | True | False | False | 8 | 0 | non-wilderness | - |
| `multi-lines:a055` | Land's End | True | False | False | 23 | 0 | non-wilderness | - |
| `multi-lines:a056` | Graveyard of Heroes (Shayzien) | True | False | False | 18 | 0 | non-wilderness | - |
| `multi-lines:a057` | Building south of giant's pit | True | False | False | 7 | 0 | non-wilderness | - |
| `multi-lines:a058` | Shayzien Lizardman Canyon | True | False | False | 8 | 0 | non-wilderness | - |
| `multi-lines:a059` | Shayzien Wall | True | False | False | 65 | 0 | non-wilderness | - |
| `multi-lines:a060` | South of Mount Quidamortem | True | False | False | 70 | 0 | non-wilderness | - |
| `multi-lines:a061` | Stranglewood (DT 2 Area) | True | False | False | 30 | 0 | non-wilderness | - |
| `multi-lines:a062` | Fortis Colosseum | False | False | False | 30 | 0 | non-wilderness,instance_or_raid | Since this is an instanced area there's no point marking it multi unless in the future that changes. |
| `multi-lines:a063` | Varlamore Wolf Den | True | False | False | 6 | 0 | non-wilderness | - |
| `multi-lines:a064` | Varlamore Main Palace Area | True | False | False | 27 | 0 | non-wilderness | - |
| `multi-lines:a065` | Varlamore Colossal Remains Area | True | False | False | 39 | 0 | non-wilderness | - |
| `multi-lines:a066` | The teomat | True | False | False | 38 | 0 | non-wilderness | - |
| `multi-lines:a067` | Sunset Coast | True | False | False | 14 | 0 | non-wilderness | - |
| `multi-lines:a068` | Dark warrior's palace | False | False | True | 1 | 0 | wilderness | Dark warrior's palace |
| `multi-lines:a069` | Rev caves | False | False | True | 1 | 0 | wilderness | Two tiles next to southern rev caves entrance which used to be a BH 'singles' lure spot |
| `multi-lines:a070` | Chaos altar | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a071` | Wilderness agility course | False | False | True | 1 | 0 | wilderness | Balance crossing to wilderness agility course |
| `multi-lines:a072` | North of kbd entrance | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a073` | KBD | False | False | True | 1 | 0 | wilderness | Two tiles NE of kbd entrance cage |
| `multi-lines:a074` | North of Rune Rocks | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a075` | North of lava maze | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a076` | Northeast f2p wilderness | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a077` | Northeast p2p wilderness | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a078` | North-mid east f2p wilderness | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a079` | East f2p wilderness | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a080` | Small east f2p wilderness strip NE of lumberyard | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a081` | One silly tile that used to be a BH 'singles' lure spot | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a082` | Wilderness north of Grand Exchange | False | False | True | 1 | 0 | wilderness | - |
| `multi-lines:a083` | Ferox Enclave | False | False | True | 9 | 0 | wilderness | - |
| `multi-lines:a084` | Ferox Enclave extra | False | False | True | 1 | 0 | wilderness | One separate tile north of bridge east of ferox |
| `wilderness-lines:a001` | Dark warrior's palace | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a002` | Two tiles next to southern rev caves entrance which used to be a BH "singles" lure spot | True | False | True | 1 | 0 | wilderness,historical_note | Source comment names rectangle. Historical BH wording; rectangle remains enabled. |
| `wilderness-lines:a003` | Chaos altar | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a004` | Balance crossing to wilderness agility course | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a005` | North of kbd entrance | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a006` | Two tiles NE of kbd entrance cage | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a007` | North of rune rocks | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a008` | North of lava maze | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a009` | Northeast of lava maze | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a010` | Northeast f2p wilderness | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a011` | Northeast p2p wilderness | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a012` | North-mid east f2p wilderness | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a013` | East f2p wilderness | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a014` | Small east f2p wilderness strip NE of lumberyard | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a015` | One silly tile that used to be a BH "singles" lure spot | True | False | True | 1 | 0 | wilderness,historical_note | Source comment names rectangle. Historical BH wording; rectangle remains enabled. |
| `wilderness-lines:a016` | Wilderness north of Grand Exchange | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a017` | SE of Ferox 1 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a018` | SE of Ferox 2 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a019` | SE of Ferox 2 extension 1 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a020` | SE of Ferox 2 extension 2 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a021` | SE of Ferox 2 extension 3 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a022` | SE of Ferox 2 extension 4 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a023` | SE of Ferox 3 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a024` | East of Ferox 1 | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a025` | East of Ferox 2, south of bridge | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a026` | East of Ferox 2, bridge and north of bridge | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a027` | Two dumb tiles north of bridge east of Ferox | True | False | True | 1 | 0 | wilderness | Source comment names rectangle. |
| `wilderness-lines:a028` | Slayer dungeon, Vet'ion, Venenatis, Callisto, and escape caves | True | False | True | 1 | 1 | wilderness,underground_or_cave | Source comment names rectangle. |
| `wilderness-lines:a029` | Scorpia's cave | True | False | True | 1 | 1 | wilderness,underground_or_cave | Source comment names rectangle. |
| `wilderness-lines:a030` | Wilderness God Wars Dungeon south portion | True | False | True | 1 | 1 | wilderness,underground_or_cave | Source comment names rectangle. |
| `wilderness-lines:a031` | Wilderness God Wars Dungeon middle portion | True | False | True | 1 | 1 | wilderness,underground_or_cave | Source comment names rectangle. |
| `wilderness-lines:a032` | Wilderness God Wars Dungeon north portion | True | False | True | 1 | 1 | wilderness,underground_or_cave | Source comment names rectangle. |

## Wilderness rectangles

| ID | Name | x | y | w | h | x2 excl | y2 excl | layer |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| `wilderness-lines:r0001` | Dark warrior's palace | 3008 | 3600 | 64 | 112 | 3072 | 3712 | 0 |
| `wilderness-lines:r0002` | Two tiles next to southern rev caves entrance which used to be a BH "singles" lure spot | 3072 | 3654 | 1 | 2 | 3073 | 3656 | 0 |
| `wilderness-lines:r0003` | Chaos altar | 2946 | 3816 | 14 | 16 | 2960 | 3832 | 0 |
| `wilderness-lines:r0004` | Balance crossing to wilderness agility course | 2984 | 3912 | 24 | 16 | 3008 | 3928 | 0 |
| `wilderness-lines:r0005` | North of kbd entrance | 3008 | 3856 | 40 | 48 | 3048 | 3904 | 0 |
| `wilderness-lines:r0006` | Two tiles NE of kbd entrance cage | 3021 | 3855 | 2 | 1 | 3023 | 3856 | 0 |
| `wilderness-lines:r0007` | North of rune rocks | 3048 | 3896 | 24 | 8 | 3072 | 3904 | 0 |
| `wilderness-lines:r0008` | North of lava maze | 3072 | 3880 | 64 | 24 | 3136 | 3904 | 0 |
| `wilderness-lines:r0009` | Northeast of lava maze | 3112 | 3872 | 24 | 8 | 3136 | 3880 | 0 |
| `wilderness-lines:r0010` | Northeast f2p wilderness | 3136 | 3840 | 256 | 64 | 3392 | 3904 | 0 |
| `wilderness-lines:r0011` | Northeast p2p wilderness | 3200 | 3904 | 192 | 64 | 3392 | 3968 | 0 |
| `wilderness-lines:r0012` | North-mid east f2p wilderness | 3152 | 3752 | 176 | 88 | 3328 | 3840 | 0 |
| `wilderness-lines:r0013` | East f2p wilderness | 3192 | 3525 | 136 | 227 | 3328 | 3752 | 0 |
| `wilderness-lines:r0014` | Small east f2p wilderness strip NE of lumberyard | 3328 | 3525 | 17 | 8 | 3345 | 3533 | 0 |
| `wilderness-lines:r0015` | One silly tile that used to be a BH "singles" lure spot | 3191 | 3689 | 1 | 1 | 3192 | 3690 | 0 |
| `wilderness-lines:r0016` | Wilderness north of Grand Exchange | 3136 | 3525 | 56 | 59 | 3192 | 3584 | 0 |
| `wilderness-lines:r0017` | SE of Ferox 1 | 3152 | 3584 | 40 | 36 | 3192 | 3620 | 0 |
| `wilderness-lines:r0018` | SE of Ferox 2 | 3146 | 3598 | 6 | 22 | 3152 | 3620 | 0 |
| `wilderness-lines:r0019` | SE of Ferox 2 extension 1 | 3147 | 3596 | 5 | 2 | 3152 | 3598 | 0 |
| `wilderness-lines:r0020` | SE of Ferox 2 extension 2 | 3149 | 3595 | 3 | 1 | 3152 | 3596 | 0 |
| `wilderness-lines:r0021` | SE of Ferox 2 extension 3 | 3150 | 3594 | 2 | 1 | 3152 | 3595 | 0 |
| `wilderness-lines:r0022` | SE of Ferox 2 extension 4 | 3151 | 3593 | 1 | 1 | 3152 | 3594 | 0 |
| `wilderness-lines:r0023` | SE of Ferox 3 | 3152 | 3620 | 10 | 6 | 3162 | 3626 | 0 |
| `wilderness-lines:r0024` | East of Ferox 1 | 3187 | 3620 | 5 | 28 | 3192 | 3648 | 0 |
| `wilderness-lines:r0025` | East of Ferox 2, south of bridge | 3179 | 3636 | 8 | 4 | 3187 | 3640 | 0 |
| `wilderness-lines:r0026` | East of Ferox 2, bridge and north of bridge | 3176 | 3640 | 11 | 8 | 3187 | 3648 | 0 |
| `wilderness-lines:r0027` | Two dumb tiles north of bridge east of Ferox | 3174 | 3647 | 2 | 1 | 3176 | 3648 | 0 |
| `wilderness-lines:r0028` | Slayer dungeon, Vet'ion, Venenatis, Callisto, and escape caves | 3264 | 10048 | 192 | 320 | 3456 | 10368 | 1 |
| `wilderness-lines:r0029` | Scorpia's cave | 3218 | 10330 | 31 | 24 | 3249 | 10354 | 1 |
| `wilderness-lines:r0030` | Wilderness God Wars Dungeon south portion | 3008 | 10112 | 64 | 28 | 3072 | 10140 | 1 |
| `wilderness-lines:r0031` | Wilderness God Wars Dungeon middle portion | 3008 | 10140 | 51 | 12 | 3059 | 10152 | 1 |
| `wilderness-lines:r0032` | Wilderness God Wars Dungeon north portion | 3008 | 10152 | 39 | 24 | 3047 | 10176 | 1 |

## Duplicate and overlap audit
- Exact duplicates (22): `multi-lines:r1019`=`wilderness-lines:r0001`, `multi-lines:r1020`=`wilderness-lines:r0002`, `multi-lines:r1021`=`wilderness-lines:r0003`, `multi-lines:r1022`=`wilderness-lines:r0004`, `multi-lines:r1023`=`wilderness-lines:r0005`, `multi-lines:r1024`=`wilderness-lines:r0006`, `multi-lines:r1025`=`wilderness-lines:r0007`, `multi-lines:r1026`=`wilderness-lines:r0008`, `multi-lines:r1027`=`wilderness-lines:r0010`, `multi-lines:r1028`=`wilderness-lines:r0011`, `multi-lines:r1029`=`wilderness-lines:r0012`, `multi-lines:r1030`=`wilderness-lines:r0013`, `multi-lines:r1032`=`wilderness-lines:r0015`, `multi-lines:r1033`=`wilderness-lines:r0016`, `multi-lines:r1034`=`wilderness-lines:r0017`, `multi-lines:r1035`=`wilderness-lines:r0018`, `multi-lines:r1036`=`wilderness-lines:r0019`, `multi-lines:r1037`=`wilderness-lines:r0020`, `multi-lines:r1038`=`wilderness-lines:r0021`, `multi-lines:r1039`=`wilderness-lines:r0022`, `multi-lines:r1040`=`wilderness-lines:r0023`, `multi-lines:r1041`=`wilderness-lines:r0024`
- Positive-area overlaps (46):
  - `multi-lines:r0038` ∩ `multi-lines:r0040`: 25 tiles, same_area=True, cross_source=False, bounds={'x_min': 3007, 'y_min': 3304, 'x_max_exclusive': 3008, 'y_max_exclusive': 3329, 'tile_count': 25}
  - `multi-lines:r0106` ∩ `multi-lines:r0119`: 1 tiles, same_area=False, cross_source=False, bounds={'x_min': 3604, 'y_min': 3426, 'x_max_exclusive': 3605, 'y_max_exclusive': 3427, 'tile_count': 1}
  - `multi-lines:r0364` ∩ `multi-lines:r0365`: 39 tiles, same_area=True, cross_source=False, bounds={'x_min': 1846, 'y_min': 3506, 'x_max_exclusive': 1885, 'y_max_exclusive': 3507, 'tile_count': 39}
  - `multi-lines:r0406` ∩ `multi-lines:r0411`: 11 tiles, same_area=True, cross_source=False, bounds={'x_min': 1736, 'y_min': 3489, 'x_max_exclusive': 1737, 'y_max_exclusive': 3500, 'tile_count': 11}
  - `multi-lines:r0433` ∩ `multi-lines:r0434`: 3 tiles, same_area=True, cross_source=False, bounds={'x_min': 1746, 'y_min': 3612, 'x_max_exclusive': 1747, 'y_max_exclusive': 3615, 'tile_count': 3}
  - `multi-lines:r0456` ∩ `multi-lines:r0460`: 41 tiles, same_area=True, cross_source=False, bounds={'x_min': 1747, 'y_min': 3688, 'x_max_exclusive': 1788, 'y_max_exclusive': 3689, 'tile_count': 41}
  - `multi-lines:r0456` ∩ `multi-lines:r0469`: 40 tiles, same_area=True, cross_source=False, bounds={'x_min': 1747, 'y_min': 3688, 'x_max_exclusive': 1787, 'y_max_exclusive': 3689, 'tile_count': 40}
  - `multi-lines:r0460` ∩ `multi-lines:r0466`: 8 tiles, same_area=True, cross_source=False, bounds={'x_min': 1745, 'y_min': 3688, 'x_max_exclusive': 1747, 'y_max_exclusive': 3692, 'tile_count': 8}
  - `multi-lines:r0460` ∩ `multi-lines:r0467`: 8 tiles, same_area=True, cross_source=False, bounds={'x_min': 1743, 'y_min': 3688, 'x_max_exclusive': 1745, 'y_max_exclusive': 3692, 'tile_count': 8}
  - `multi-lines:r0460` ∩ `multi-lines:r0468`: 8 tiles, same_area=True, cross_source=False, bounds={'x_min': 1741, 'y_min': 3688, 'x_max_exclusive': 1743, 'y_max_exclusive': 3692, 'tile_count': 8}
  - `multi-lines:r0460` ∩ `multi-lines:r0469`: 184 tiles, same_area=True, cross_source=False, bounds={'x_min': 1741, 'y_min': 3688, 'x_max_exclusive': 1787, 'y_max_exclusive': 3692, 'tile_count': 184}
  - `multi-lines:r0461` ∩ `multi-lines:r0469`: 280 tiles, same_area=True, cross_source=False, bounds={'x_min': 1747, 'y_min': 3692, 'x_max_exclusive': 1787, 'y_max_exclusive': 3699, 'tile_count': 280}
  - `multi-lines:r0462` ∩ `multi-lines:r0469`: 40 tiles, same_area=True, cross_source=False, bounds={'x_min': 1747, 'y_min': 3699, 'x_max_exclusive': 1787, 'y_max_exclusive': 3700, 'tile_count': 40}
  - `multi-lines:r0466` ∩ `multi-lines:r0469`: 24 tiles, same_area=True, cross_source=False, bounds={'x_min': 1745, 'y_min': 3688, 'x_max_exclusive': 1747, 'y_max_exclusive': 3700, 'tile_count': 24}
  - `multi-lines:r0467` ∩ `multi-lines:r0469`: 24 tiles, same_area=True, cross_source=False, bounds={'x_min': 1743, 'y_min': 3688, 'x_max_exclusive': 1745, 'y_max_exclusive': 3700, 'tile_count': 24}
  - `multi-lines:r0468` ∩ `multi-lines:r0469`: 24 tiles, same_area=True, cross_source=False, bounds={'x_min': 1741, 'y_min': 3688, 'x_max_exclusive': 1743, 'y_max_exclusive': 3700, 'tile_count': 24}
  - `multi-lines:r0469` ∩ `multi-lines:r0470`: 6 tiles, same_area=True, cross_source=False, bounds={'x_min': 1739, 'y_min': 3692, 'x_max_exclusive': 1741, 'y_max_exclusive': 3695, 'tile_count': 6}
  - `multi-lines:r0967` ∩ `multi-lines:r0968`: 7 tiles, same_area=True, cross_source=False, bounds={'x_min': 1445, 'y_min': 3213, 'x_max_exclusive': 1452, 'y_max_exclusive': 3214, 'tile_count': 7}
  - `multi-lines:r0968` ∩ `multi-lines:r0969`: 11 tiles, same_area=True, cross_source=False, bounds={'x_min': 1442, 'y_min': 3211, 'x_max_exclusive': 1453, 'y_max_exclusive': 3212, 'tile_count': 11}
  - `multi-lines:r0972` ∩ `multi-lines:r0973`: 20 tiles, same_area=True, cross_source=False, bounds={'x_min': 1436, 'y_min': 3202, 'x_max_exclusive': 1456, 'y_max_exclusive': 3203, 'tile_count': 20}
  - `multi-lines:r1019` ∩ `wilderness-lines:r0001`: 7168 tiles, same_area=False, cross_source=True, bounds={'x_min': 3008, 'y_min': 3600, 'x_max_exclusive': 3072, 'y_max_exclusive': 3712, 'tile_count': 7168}
  - `multi-lines:r1020` ∩ `wilderness-lines:r0002`: 2 tiles, same_area=False, cross_source=True, bounds={'x_min': 3072, 'y_min': 3654, 'x_max_exclusive': 3073, 'y_max_exclusive': 3656, 'tile_count': 2}
  - `multi-lines:r1021` ∩ `wilderness-lines:r0003`: 224 tiles, same_area=False, cross_source=True, bounds={'x_min': 2946, 'y_min': 3816, 'x_max_exclusive': 2960, 'y_max_exclusive': 3832, 'tile_count': 224}
  - `multi-lines:r1022` ∩ `wilderness-lines:r0004`: 384 tiles, same_area=False, cross_source=True, bounds={'x_min': 2984, 'y_min': 3912, 'x_max_exclusive': 3008, 'y_max_exclusive': 3928, 'tile_count': 384}
  - `multi-lines:r1023` ∩ `wilderness-lines:r0005`: 1920 tiles, same_area=False, cross_source=True, bounds={'x_min': 3008, 'y_min': 3856, 'x_max_exclusive': 3048, 'y_max_exclusive': 3904, 'tile_count': 1920}
  - `multi-lines:r1024` ∩ `wilderness-lines:r0006`: 2 tiles, same_area=False, cross_source=True, bounds={'x_min': 3021, 'y_min': 3855, 'x_max_exclusive': 3023, 'y_max_exclusive': 3856, 'tile_count': 2}
  - `multi-lines:r1025` ∩ `wilderness-lines:r0007`: 192 tiles, same_area=False, cross_source=True, bounds={'x_min': 3048, 'y_min': 3896, 'x_max_exclusive': 3072, 'y_max_exclusive': 3904, 'tile_count': 192}
  - `multi-lines:r1026` ∩ `wilderness-lines:r0008`: 1536 tiles, same_area=False, cross_source=True, bounds={'x_min': 3072, 'y_min': 3880, 'x_max_exclusive': 3136, 'y_max_exclusive': 3904, 'tile_count': 1536}
  - `multi-lines:r1027` ∩ `wilderness-lines:r0010`: 16384 tiles, same_area=False, cross_source=True, bounds={'x_min': 3136, 'y_min': 3840, 'x_max_exclusive': 3392, 'y_max_exclusive': 3904, 'tile_count': 16384}
  - `multi-lines:r1028` ∩ `wilderness-lines:r0011`: 12288 tiles, same_area=False, cross_source=True, bounds={'x_min': 3200, 'y_min': 3904, 'x_max_exclusive': 3392, 'y_max_exclusive': 3968, 'tile_count': 12288}
  - `multi-lines:r1029` ∩ `wilderness-lines:r0012`: 15488 tiles, same_area=False, cross_source=True, bounds={'x_min': 3152, 'y_min': 3752, 'x_max_exclusive': 3328, 'y_max_exclusive': 3840, 'tile_count': 15488}
  - `multi-lines:r1030` ∩ `wilderness-lines:r0013`: 30872 tiles, same_area=False, cross_source=True, bounds={'x_min': 3192, 'y_min': 3525, 'x_max_exclusive': 3328, 'y_max_exclusive': 3752, 'tile_count': 30872}
  - `multi-lines:r1031` ∩ `wilderness-lines:r0014`: 136 tiles, same_area=False, cross_source=True, bounds={'x_min': 3328, 'y_min': 3525, 'x_max_exclusive': 3345, 'y_max_exclusive': 3533, 'tile_count': 136}
  - `multi-lines:r1032` ∩ `wilderness-lines:r0015`: 1 tiles, same_area=False, cross_source=True, bounds={'x_min': 3191, 'y_min': 3689, 'x_max_exclusive': 3192, 'y_max_exclusive': 3690, 'tile_count': 1}
  - `multi-lines:r1033` ∩ `wilderness-lines:r0016`: 3304 tiles, same_area=False, cross_source=True, bounds={'x_min': 3136, 'y_min': 3525, 'x_max_exclusive': 3192, 'y_max_exclusive': 3584, 'tile_count': 3304}
  - `multi-lines:r1034` ∩ `wilderness-lines:r0017`: 1440 tiles, same_area=False, cross_source=True, bounds={'x_min': 3152, 'y_min': 3584, 'x_max_exclusive': 3192, 'y_max_exclusive': 3620, 'tile_count': 1440}
  - `multi-lines:r1035` ∩ `wilderness-lines:r0018`: 132 tiles, same_area=False, cross_source=True, bounds={'x_min': 3146, 'y_min': 3598, 'x_max_exclusive': 3152, 'y_max_exclusive': 3620, 'tile_count': 132}
  - `multi-lines:r1036` ∩ `wilderness-lines:r0019`: 10 tiles, same_area=False, cross_source=True, bounds={'x_min': 3147, 'y_min': 3596, 'x_max_exclusive': 3152, 'y_max_exclusive': 3598, 'tile_count': 10}
  - `multi-lines:r1037` ∩ `wilderness-lines:r0020`: 3 tiles, same_area=False, cross_source=True, bounds={'x_min': 3149, 'y_min': 3595, 'x_max_exclusive': 3152, 'y_max_exclusive': 3596, 'tile_count': 3}
  - `multi-lines:r1038` ∩ `wilderness-lines:r0021`: 2 tiles, same_area=False, cross_source=True, bounds={'x_min': 3150, 'y_min': 3594, 'x_max_exclusive': 3152, 'y_max_exclusive': 3595, 'tile_count': 2}
  - `multi-lines:r1039` ∩ `wilderness-lines:r0022`: 1 tiles, same_area=False, cross_source=True, bounds={'x_min': 3151, 'y_min': 3593, 'x_max_exclusive': 3152, 'y_max_exclusive': 3594, 'tile_count': 1}
  - `multi-lines:r1040` ∩ `wilderness-lines:r0023`: 60 tiles, same_area=False, cross_source=True, bounds={'x_min': 3152, 'y_min': 3620, 'x_max_exclusive': 3162, 'y_max_exclusive': 3626, 'tile_count': 60}
  - `multi-lines:r1041` ∩ `wilderness-lines:r0024`: 140 tiles, same_area=False, cross_source=True, bounds={'x_min': 3187, 'y_min': 3620, 'x_max_exclusive': 3192, 'y_max_exclusive': 3648, 'tile_count': 140}
  - `multi-lines:r1042` ∩ `wilderness-lines:r0025`: 32 tiles, same_area=False, cross_source=True, bounds={'x_min': 3179, 'y_min': 3636, 'x_max_exclusive': 3187, 'y_max_exclusive': 3640, 'tile_count': 32}
  - `multi-lines:r1042` ∩ `wilderness-lines:r0026`: 88 tiles, same_area=False, cross_source=True, bounds={'x_min': 3176, 'y_min': 3640, 'x_max_exclusive': 3187, 'y_max_exclusive': 3648, 'tile_count': 88}
  - `multi-lines:r1043` ∩ `wilderness-lines:r0027`: 1 tiles, same_area=False, cross_source=True, bounds={'x_min': 3175, 'y_min': 3647, 'x_max_exclusive': 3176, 'y_max_exclusive': 3648, 'tile_count': 1}
