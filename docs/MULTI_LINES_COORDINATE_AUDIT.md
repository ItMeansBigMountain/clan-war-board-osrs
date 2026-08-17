# Multi-Lines coordinate and boundary audit

Inspected revisions:

- `Nightfirecat/plugin-hub-plugins` at `d81bb4c444c3772b1563be8a8fcc2252c84a0a4f`
- `tsbreuer/Multi-Lines` at `858246aa7381584c81b1a9a94c67af595a75a51a`

## Coordinate model

Both plugins model multi-combat space as unions of axis-aligned world-tile rectangles using `java.awt.Rectangle` and `java.awt.geom.Area`. Rectangle fields are `(worldX, worldY, width, height)`, and adjacent or overlapping rectangles are merged.

- [Wilderness Lines coordinate data](https://github.com/Nightfirecat/plugin-hub-plugins/blob/d81bb4c444c3772b1563be8a8fcc2252c84a0a4f/src/main/java/at/nightfirec/wildernesslines/WildernessLinesPlugin.java#L74-L107)
- [Multi-Lines JSON loader](https://github.com/tsbreuer/Multi-Lines/blob/858246aa7381584c81b1a9a94c67af595a75a51a/src/main/java/com/tsbreuer/multilines/MultiLinesPlugin.java#L137-L193)
- [Multi-Lines area dataset](https://github.com/tsbreuer/Multi-Lines/blob/858246aa7381584c81b1a9a94c67af595a75a51a/src/main/java/com/tsbreuer/multilines/MultiLinesData.json)

Multi-Lines groups rectangles into named areas with `Name`, `Removed`, `Enabled`, `Wilderness`, `Notes`, and `Tiles`. Surface and underground Wilderness coordinates coexist in the Wilderness source; caves use a different coordinate space near `y=10000`. Plane is not encoded in the rectangle itself, so Clan War Board must explicitly store coordinate layer, plane, and world mode.

## Boundary rendering

Wilderness Lines:

1. Unions rectangles into an `Area`.
2. Converts the union to a path.
3. Clips it to the current scene.
4. Splits edges into one-tile segments.
5. Converts world coordinates to local/canvas coordinates.

- [Boundary extraction](https://github.com/Nightfirecat/plugin-hub-plugins/blob/d81bb4c444c3772b1563be8a8fcc2252c84a0a4f/src/main/java/at/nightfirec/wildernesslines/WildernessLinesPlugin.java#L324-L367)
- [World-to-local boundary alignment](https://github.com/Nightfirecat/plugin-hub-plugins/blob/d81bb4c444c3772b1563be8a8fcc2252c84a0a4f/src/main/java/at/nightfirec/wildernesslines/WildernessLinesPlugin.java#L324-L329)

Multi-Lines simplifies projected display paths using a floating-point slope tolerance. This must not be used for authoritative containment; it is display-only.

- [Multi-Lines overlay simplification](https://github.com/tsbreuer/Multi-Lines/blob/858246aa7381584c81b1a9a94c67af595a75a51a/src/main/java/com/tsbreuer/multilines/MultiLinesOverlay.java#L77-L181)

## Live validation and edge cases

The Wilderness source checks its static map against the in-game multiway indicator and contains exceptions for known forced movement and transition tiles. The data includes tiny `1x1`, `1x2`, and `2x1` islands, thin Ferox extensions, overlapping corrections, caves, and irregular edges. A region-only or coarse bounding-box test is therefore unsafe.

- [Live multiway comparison](https://github.com/Nightfirecat/plugin-hub-plugins/blob/d81bb4c444c3772b1563be8a8fcc2252c84a0a4f/src/main/java/at/nightfirec/wildernesslines/WildernessLinesPlugin.java#L241-L283)
- [Known mismatch exceptions](https://github.com/Nightfirecat/plugin-hub-plugins/blob/d81bb4c444c3772b1563be8a8fcc2252c84a0a4f/src/main/java/at/nightfirec/wildernesslines/WildernessLinesPlugin.java#L110-L112)

## Clan War Board implementation contract

Represent a venue as an immutable, versioned object containing:

- Stable ID and name
- World mode: standard Wilderness or PvP world
- Coordinate layer and plane
- Exact rectangle/tile runs
- Enabled/removed flags
- Source revision and verification date
- Hill polygon and minimum inward clearance
- Ingress, exit, hazard, Wilderness-level, and combat-range metadata

Compile each venue into:

1. An exact tile-membership set for validation.
2. Boundary segments for rendering.
3. Deduplicated candidate tiles for random selection.

Random selection must sample uniformly from deduplicated valid tiles, not rectangles, because rectangles overlap and have unequal sizes. For KOTH, erode the valid multi union by the required hill radius before sampling so every point of the hill stays inside multi.

At runtime, a hill-control heartbeat counts only when the local player is inside the accepted venue polygon and the live multiway indicator agrees. Static geometry chooses candidates; live game state remains the final event-time check.

## Licensing

Both inspected projects use BSD-2-Clause. Any copied/adapted code or coordinate dataset must retain the relevant copyright notice, conditions, and disclaimer.

- [Wilderness Lines license](https://github.com/Nightfirecat/plugin-hub-plugins/blob/d81bb4c444c3772b1563be8a8fcc2252c84a0a4f/LICENSE)
- [Multi-Lines license](https://github.com/tsbreuer/Multi-Lines/blob/858246aa7381584c81b1a9a94c67af595a75a51a/LICENSE)
