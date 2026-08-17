# Changelog

All notable changes to EventLens are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-17

### Added

- Client mods: clickable `/eventlens` status, listeners, and trace start/stop/list/view/export
- Client trace events beyond tick: chat, screens, attack, use item/block/entity/empty, move, join/disconnect, respawn, pause, game mode, input, tooltip, screenshot, toast, sound, entity/chunk load, world/player tick, recipes
- Optional `eventlens-client-agent` for NeoForge and Forge per-mod handler timing (`docs/adr/0003-client-java-agent.md`)
- `/eventlens trace restart <session>` clones a stopped session's filters into a new session ID
- MIT license
- Client `/eventlens` status shows `precise` vs `dispatch-only`; `trace view` lists handler rows when the agent is attached
- NeoForge/Forge in-game EventLens Screen and optional HUD (`/eventlens ui`, keybinds, `[Open UI]`)
- EventLens Screen restyled as a floating diagnostic panel with status pills, tab underline, and inset lists
- EventLens Screen search on Events, Sessions, and Session; taller list rows; even footer actions; steel outline instead of a cyan spine
- Client session pause/resume (`/eventlens trace pause|resume`, Screen buttons) and hover details on live/idle and precise/dispatch pills
- `eventlens-forge` now uses Minecraft Forge 1.21.1 (`:eventlens-forge:runClient`) instead of launching NeoForge
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

### Fixed

- Forge `runClient` now loads EventLens as one mod file (classes + `mods.toml`) and includes `pack.mcmeta`
- Forge `[Open UI]` chat clicks now run `/eventlens ui` on the client instead of sending it to the server
- Forge `runClient` attaches the client agent so instrumentation can report `precise` instead of dispatch-only
- Paper plugin JAR now shades `eventlens-core`, so `/eventlens` loads on a local `runServer`
- `/eventlens` with no arguments shows status instead of an incomplete-command error
- Forge `[Open UI]` opens the Screen directly and swallows EventLens chat clicks before they reach the server
