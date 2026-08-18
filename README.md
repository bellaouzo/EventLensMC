# EventLens

EventLens is a Paper **26.2** server plugin that diagnoses how Minecraft events travel through registered plugin listeners. It observes server behavior without changing it.

Use it to answer questions such as:

- Which plugins listen to an event, and in what order?
- Which listener changed cancellation state or event properties?
- Which listener is slow or threw an exception?
- How did a specific dispatch flow through the listener chain?

Alias: `/el`

## Requirements

- **Java 25** JDK
- **Paper 26.2** server
- Gradle Wrapper (`gradlew.bat`) — no global Gradle install required

Set `JAVA_HOME` to your Java 25 installation. Windows PowerShell is the primary development shell, but any shell that can run `gradlew.bat` works.

## Quick start

```powershell
.\gradlew.bat clean build
.\gradlew.bat check
.\gradlew.bat runServer
```

On first server start, accept the Minecraft EULA in `run/eula.txt` when prompted.

Built plugin JAR: `eventlens-paper/build/libs/EventLens-1.2.4.jar`

Development server directory: `run/` (gitignored)

### Java agent (per-listener timing)

Per-listener timing, slow-listener rankings, and exception attribution require the optional **EventLens Java agent** (`eventlens-agent` subproject). Development `runServer` and `runServerDebug` attach it automatically via `-javaagent`.

Without the agent, EventLens still reports dispatch-level timing and listener inventory; per-listener breakdowns are unavailable.

## Commands

All commands default to **op** permission unless noted.

### `/eventlens status`

Reports plugin version, platform, tracing state, active session count, and agent status.

Permission: `eventlens.command.status`

### `/eventlens listeners <event> [page] [--detail brief|normal|verbose]`

Lists registered listeners for any Bukkit event type, with plugin, priority, and handler method. Supports pagination and optional conflict hints at higher detail levels.

Permission: `eventlens.command.listeners`

### `/eventlens plugin <plugin> [listeners [event] [page]]`

Shows a plugin profile: listener counts by event and priority, trace-derived timing/exception stats, load state, dependencies, and recent attributed changes.

```
/eventlens plugin compare <pluginA> <pluginB>
```

Compare works only with **loaded** plugins.

Permission: `eventlens.command.plugin`

### `/eventlens trace`

Bounded trace sessions capture event snapshots, listener-order diffs, timing, and cancellation transitions for supported event types.

**Subcommands:** `start`, `stop`, `list`, `view`, `live`, `export`, `copy`, `compare`, `correlate`, `history`, `favorite`, `presets`

Permission: `eventlens.command.trace` (granular child permissions per subcommand in `plugin.yml`)

Also: `/eventlens events [prefix]` (traceable vs generic-only vs hot) and `/eventlens exceptions [page]`.

#### Trace start

```
/eventlens trace start <EventSimpleName[,EventSimpleName...]> [--plugin <name>] [--preset <name>]
  [--generic] [--max-duration 60s] [--max-events 256] [--slow-threshold 1ms] [--capture-stacks]
```

Allowlisted types get first-class snapshots. A registered event outside that list needs `--generic` (common fields only). `listeners` still resolves any registered Bukkit event.

Hot events such as `PlayerMoveEvent` may require explicit confirmation (`eventlens.command.trace.hot-event`) and a narrowing filter.

```
/eventlens trace correlate <serverSession> <clientSession>
```

Joins Paper and client reports by correlation key. Live linking uses the optional `eventlens:correlate` plugin channel when the client mod is present.

#### Trace view and export

```
/eventlens trace stop [sessionId]
/eventlens trace view <sessionId> [--detail brief|normal|verbose]
/eventlens trace export <sessionId> [--format json|markdown|bundle] [--full]
/eventlens trace copy <sessionId>
/eventlens trace compare <sessionA> <sessionB>
```

Exports are redacted by default. `--full` requires `eventlens.command.trace.export.full`. A `bundle` export is a folder with `index.html` (open this) and a pretty-printed viewer `report.json`. Use `--format json` for the full snapshot dump.

#### Live feed

Attach a real-time feed to an active session (in-game player required):

```
/eventlens trace live <sessionId> [--channels frequency,slow,cancel,exception,alert]
  [--display chat|actionbar|bossbar] [--filter-plugin <name>] [--threshold 1ms]
  [--burst 50] [--aggregate 3s]
/eventlens trace live status|stop|pause|resume
/eventlens trace live [--filter-plugin <name>]   # update filters on current subscription
```

Live channels include aggregated event frequency, slow-listener alerts, cancellation transitions, exceptions, and plugin burst detection. Filters and pause/resume apply without restarting the trace.

Permission: `eventlens.command.trace.live`

#### Preferences

```
/eventlens trace history
/eventlens trace favorite list|add|remove <event>
/eventlens trace presets
```

## Configuration

`plugins/EventLens/config.yml` (defaults merged on startup):

| Section | Purpose |
|---|---|
| `reports` | Export retention and auto-cleanup |
| `output.detail-level` | Default output verbosity |
| `trace.slow-threshold-default` | Default slow-listener threshold |
| `trace.require-hot-event-confirmation` | Gate high-frequency events |
| `trace.live.*` | Live feed aggregation, burst detection, display mode |
| `trace.presets` | Named trace presets (`quick-interact`, `plugin-focus`, …) |
| `preferences` | Recent trace and favorite limits |

Resource limits and redaction defaults are summarized in [CHANGELOG.md](CHANGELOG.md) and the local `AGENTS.md` / `docs/` files (gitignored).

## Development

```powershell
.\gradlew.bat check          # tests, Spotless, SonarLint, ArchUnit, file-length checks
.\gradlew.bat paperSmokeTest # headless Paper startup + command smoke checks
.\gradlew.bat runServerDebug  # Paper server with debug port 127.0.0.1:5005
.\gradlew.bat --stop          # stop Gradle daemons
```

Optional smoke-test helper: `.\scripts\smoke-test.ps1`
Full validation helper: `.\scripts\smoke-test-full.ps1`

Stop the test server with `stop` in the console — never `/reload`.

## Project layout

| Path | Role |
|---|---|
| `src/main/java` | Paper plugin (commands, application services, domain) |
| `eventlens-observability` | Shared observation DTOs and SPI |
| `eventlens-agent` | Optional Paper `-javaagent` for per-listener instrumentation |
| `eventlens-client-agent` | Optional NeoForge client `-javaagent` for per-mod handler timing |
| `docs/` | Local setup, architecture, testing, and ADRs (gitignored) |
| `AGENTS.md` | Local agent handbook (gitignored) |

## Documentation

Published with the repo: [Changelog](CHANGELOG.md).

`AGENTS.md` and the `docs/` folder stay on disk for local work and are not on the remote.

## License

MIT. See [LICENSE](LICENSE).
