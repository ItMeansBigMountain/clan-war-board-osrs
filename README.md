# Competition Overlay

Competition Overlay is a RuneLite external plugin scaffold for OSRS clan or community competitions. It currently provides a small in-client reminder on login and a stable place to wire future competition standings/progress data into an overlay or panel.

## Current features

- RuneLite plugin metadata and package names aligned under `com.itmeansbigmountain.competitionoverlay`.
- Configurable competition name, login reminder toggle, and status message.
- Login chat reminder that formats the configured competition/status text.
- Lightweight JUnit smoke tests for plugin descriptor metadata, config defaults, and login message formatting.

## Configuration

Open RuneLite's plugin configuration after loading the plugin and adjust:

- `Competition Name`: label shown in the login reminder.
- `Show Login Message`: enables/disables the login reminder.
- `Status Message`: temporary status text shown until live standings are implemented.

## API usage notes

This repo does not currently call external APIs. Future standing/progress integration should run off the game thread, cache responses, handle Wise Old Man/TempleOSRS rate limits gracefully, and fail closed with a clear user-facing status message instead of blocking RuneLite.

## Build, test, and run locally

Use Java 11 for RuneLite compatibility:

```bash
export JAVA_HOME=/opt/data/jdks/current-java11
./gradlew test --no-daemon -q
./gradlew assemble --no-daemon -q
```

To launch RuneLite in developer mode with this external plugin loaded:

```bash
export JAVA_HOME=/opt/data/jdks/current-java11
./gradlew run --no-daemon
```

## Manual RuneLite testing checklist

1. Launch with `./gradlew run --no-daemon`.
2. Confirm the plugin appears as `Competition Overlay` in the plugin list.
3. Toggle `Show Login Message`, edit the competition name/status text, and verify the login message respects the settings.
4. Confirm startup/shutdown logs contain the plugin name and no errors are emitted.
5. Capture screenshots/GIFs before plugin-hub submission once a visual overlay or panel exists.

## Plugin-hub prep status

Ready for local manual smoke testing and further feature work. Before plugin-hub submission, add the final competition data source/overlay behavior, update this README with screenshots, and verify the support URL points at the published repository.
