# Clan War Board

Clan War Board is a RuneLite external plugin for OSRS clans that want a simple in-client war board for setting up wilderness fights.

The first version keeps the idea intentionally simple:

- RuneLite detects your rank in your current clan.
- Players at or above the configured leader rank get a **leader setup view**.
- Everyone else gets a **member rally view**.
- Leaders configure the fight details in RuneLite plugin config.
- Members see the current opponent, time, world, hotspot, and rules/notes.

This is not an enemy tracker or scouting tool. It is a clan war organization board.

## Current features

- Plugin display name: `Clan War Board`.
- Clan-rank gate using RuneLite's clan channel rank data.
- Configurable minimum leader rank:
  - Administrator
  - Deputy owner
  - Owner
- Leader setup card for eligible ranks.
- Member read-only rally card for non-leaders.
- Configurable war details:
  - war name
  - opponent clan
  - date/time
  - world
  - wilderness hotspot/rally zone
  - rules/notes
- Optional login reminder showing the active war plan and whether the user has leader setup access.
- Unit tests for plugin metadata, defaults, rank gating, and login message formatting.

## Intended product direction

Clan War Board should help clans set up and manage fights without pretending OSRS provides an official wilderness-war winner.

Near-term direction:

1. Keep leader/member UI split based on clan rank.
2. Let leaders configure/share upcoming fights.
3. Let members quickly check where to rally and what the rules are.
4. Later, add local war-session summaries such as attendance, hotspot presence, and time-in-zone.

Possible future additions:

- saved war sessions
- hotspot presets for common multi wilderness locations
- start/stop local war tracking
- post-war summary card
- copy-to-clipboard war plan
- optional small overlay with world/hotspot/time

## Configuration

Open RuneLite's plugin configuration after loading the plugin and adjust:

- `Leader Rank Needed`: minimum clan rank required to see leader setup mode.
- `War Name`: name of the planned fight.
- `Opponent Clan`: clan you are fighting.
- `Date / Time`: planned date and time.
- `World`: target world.
- `Hotspot`: rally/war zone such as Lava Dragons, Chaos Altar, or Vet'ion.
- `Rules / Notes`: gear, returns, and rule reminders for members.
- `Show Login Message`: enables/disables login reminders.

## How the leader/member split works

The plugin reads your current clan channel and tries to find your local player name in that clan. If your rank value is at or above the configured threshold, the side panel shows leader setup mode.

Default threshold:

```text
Administrator or higher
```

If you are not in a clan, your rank cannot be detected, or your rank is below the threshold, the plugin shows member view.

## Build, test, and run locally

Use Java 11 for RuneLite compatibility:

```bash
export JAVA_HOME=/opt/data/jdks/current-java11
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew clean test assemble --no-daemon --console=plain
```

To launch RuneLite in developer mode with this external plugin loaded:

```bash
export JAVA_HOME=/opt/data/jdks/current-java11
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew run --no-daemon --console=plain
```

## Manual RuneLite testing checklist

1. Launch with `./gradlew run --no-daemon --console=plain`.
2. Confirm the plugin appears as `Clan War Board` in the plugin list/sidebar.
3. Join/log into an account with a clan rank.
4. Set `Leader Rank Needed` to a rank at or below your clan rank and confirm leader setup mode appears.
5. Set `Leader Rank Needed` above your clan rank and confirm member view appears.
6. Edit war name, opponent, date/time, world, hotspot, and rules; confirm the panel and login message reflect those values.
7. Confirm startup/shutdown logs contain the plugin name and no errors are emitted.

## Plugin-hub prep status

Still in progress. Before Plugin Hub submission, add screenshots, polish the panel visually inside RuneLite, verify rank detection live with a real clan account, and decide whether v1 should include saved war sessions or only a config-backed war board.
