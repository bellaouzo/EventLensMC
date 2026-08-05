# EventLens Testing

## Current test stack

| Tool | Status |
|---|---|
| JUnit 5 | Configured in `build.gradle` |
| JUnit Platform launcher | `testRuntimeOnly` dependency present |
| ArchUnit | Configured (`ArchitectureTest`) |
| SonarLint | Configured (`name.remal.sonarlint`; `sonarLintMain`, `sonarLintTest` in `check`) |
| File length limits | Configured (`checkFileLengths` in `check`; see `file-organization.mdc`) |
| MockBukkit | Configured (`mockbukkit-v26.1.2`) |
| JaCoCo | Configured (`jacocoTestReport` after `test`) |
| GitHub Actions | `.github/workflows/ci.yml` |

Run tests:

```powershell
.\gradlew.bat test
.\gradlew.bat check
```

Reports:

- Tests: `build/reports/tests/test/index.html`
- Coverage: `build/reports/jacoco/test/html/index.html`
- SonarLint: `build/reports/sonarlint/` (HTML/XML per source set)

`check` runs SonarLint against main and test sources with all rules enabled. **Info**, **warning**, and **error** issues fail the build. Reports are written to `build/reports/sonarlint/`.

```powershell
.\gradlew.bat sonarLintMain sonarLintTest --warning-mode all
```

## Existing tests

- `TraceSessionManagerTest` — default tracing-disabled state and `closeAll()` reset behavior
- `StatusQueryServiceTest` — status query reflects session manager defaults
- `ArchitectureTest` — domain, trace, and application packages do not depend on Paper; single `JavaPlugin` subclass

## Testing matrix

### Pure unit tests (required as domain grows)

- Session state machine transitions
- Ring buffer overflow policy
- Sampling and compiled filters
- Snapshot adapter output and truncation
- Diff engine determinism
- Redaction and privacy markers
- Performance budget / throttling decisions

### Architecture tests

Enforced with ArchUnit in `ArchitectureTest`:

- No Paper imports in `domain`, `trace`, or `application` packages
- Single `JavaPlugin` subclass (`EventLens`)
- Instrumentation implementations not referenced from domain (when packages exist)

### MockBukkit

Use for command registration, permissions, and simple lifecycle — not for listener dispatch order proofs.

Current baseline coverage:

- `EventLensCommandPermissionTest` validates top-level command permission-denied behavior for non-op players

### Real Paper integration

```powershell
.\gradlew.bat runServer
.\gradlew.bat paperSmokeTest
```

Verify in `run/logs/latest.log`:

- EventLens enables without stack traces
- Commands behave as expected
- Clean disable on `stop`

Never use `/reload`.

## Manual smoke test (optional)

Build and start the dev server with the checklist printed first:

```powershell
.\scripts\smoke-test.ps1
.\scripts\smoke-test-full.ps1
```

Or build and start separately:

```powershell
.\gradlew.bat jar
.\gradlew.bat runServer
```

When the server console shows `Done`, run:

```
eventlens status
eventlens listeners PlayerJoinEvent
eventlens listeners BlockBreakEvent
eventlens trace start BlockBreakEvent --max-events 50 --max-duration 60s
eventlens trace list
eventlens trace view <sessionId>
eventlens trace stop
```

In-game (op): `/eventlens status`, `/eventlens listeners PlayerJoinEvent`

Verify:

- Status reports version, platform, tracing flag, and session count
- Listeners shows plugin, class, method, priority, `#N` order, and `ignoreCancelled`
- Partial name `PlayerJoin` resolves or lists ambiguous matches for **listeners**
- `trace start` only accepts the initial supported event set (see `SupportedEventTypes`); tab-complete lists those names
- `trace start PlayerJoinEvent` fails with an unsupported-event message
- `run/logs/latest.log` has no stack traces; `EventLens disabled` after `stop`

### Future fixture plugin

`EventLensTestTarget` (planned) will register deterministic listeners at multiple priorities for cancellation, delay, and exception scenarios. It must not ship in the release JAR.

### Future agent tests

Forked JVM tests with `-javaagent` when an agent subproject exists.

## Definition of test completeness

A feature is not done without tests appropriate to its layer. MockBukkit alone is insufficient for dispatch-order or timing claims.
