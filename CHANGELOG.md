# Changelog

All notable changes to EventLens are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1-SNAPSHOT] - Unreleased

### Added

- Gradle subprojects `eventlens-observability` and `eventlens-agent`
- Nanosecond dispatch timing and session percentile aggregates (p50/p95/p99)
- Per-listener timing via Java agent instrumentation of `RegisteredListener.callEvent`
- Per-listener before/after snapshots, property changes, and cancellation timeline (agent + snapshot bridge)
- Instrumentation diagnostics in `/eventlens status` (mode, capabilities, Paper compatibility, fallback guidance)
- Plugin-only fallback to priority-band snapshots when agent is absent or degraded
- Slow listener/plugin rankings, main-thread blocking detection, frequency warnings
- `--slow-threshold` and `--capture-stacks` trace start options
- Performance budget auto-throttle/auto-stop and hot-event sampling for `PlayerMoveEvent`
- Timing summary in `/eventlens trace view`; agent status in `/eventlens status`
- ADR 0001 for Java agent listener timing
- MockBukkit test dependency (`org.mockbukkit.mockbukkit:mockbukkit-v26.1.2`)
- `EventLensCommandPermissionTest` for top-level command permission denials
- `paperSmokeTest` Gradle task for headless Paper command smoke validation
- Windows CI smoke job running `paperSmokeTest`
- `docs/DEBUGGING_PLUGINS.md` and `docs/SMOKE_TEST.md`
- `docs/schemas/trace-report-v2.schema.json`
- `scripts/smoke-test-full.ps1`

### Changed

- Pinned Paper API dependency to `26.2.build.92-stable`
- Replaced deprecated `getDescription()` usage with `getPluginMeta()`
- Added Spotless formatting for Java sources and Markdown docs; enforced via `check`
- `runServer` / `runServerDebug` attach `eventlens-agent` automatically in development
- Updated README, AGENTS guide, and roadmap to reflect smoke automation and MockBukkit coverage
