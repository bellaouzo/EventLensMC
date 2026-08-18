# Changelog

All notable changes to EventLens are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.4] - 2026-08-17

### Changed

- Bundle `report.json` is a viewer summary (session, timings, dispatch facts). Use `--format json` for the full snapshot dump

## [1.2.3] - 2026-08-17

### Changed

- Bundle export is two files: open `index.html` (self-contained viewer) and inspect `report.json` (pretty-printed)

### Fixed

- Bundle `index.html` now embeds the viewer and report so it opens from `file://` instead of a blank dark page

## [1.2.2] - 2026-08-17

### Added

- `/eventlens trace stop [sessionId]` stops one open session when an id is given; omit it to stop all of yours

### Fixed

- Tab-complete open session ids after `/eventlens trace stop`
- Bundle export no longer crashes when the plugin JAR path contains a space

## [1.2.1] - 2026-08-17

### Fixed

- Tab-complete extra event names after a comma in `/eventlens trace start`
- Multi-event start now traces every listed type, including names typed after a comma or space
- Bundle export copies the dashboard JS/CSS and writes a compact `report.json` so `index.html` opens offline

## [1.2.0] - 2026-08-17

### Added

- Client–server correlation keys, optional `eventlens:correlate` plugin channel, `/eventlens trace correlate`, and Linked lines when a dispatch has a peer
- Multi-event sessions (`/eventlens trace start Interact,Break`) and `block-flow` / `inventory-flow` / `chat-flow` presets
- Narrative summary on `trace view` (who cancelled, who threw, partial reasons)
- Snapshot adapters for inventory open/close/drag, drop/pickup, projectile launch/hit, creature spawn, and server command
- `/eventlens events` catalog (`traceable` / `generic-only` / `hot`) and `trace start --generic`
- Client `--mod` / `--player` start filters, `/eventlens mod`, `/eventlens trace live` notice, and client baselines
- `/eventlens exceptions` inbox and per-dispatch server tick / MSPT / client tick fields
- `--format bundle` shareable viewer folder and a Compare tab in the dashboard viewer
- Fabric Screen, HUD, keybinds, `trace pause|resume`, and `/eventlens ui`

### Changed

- Trace reports may include optional correlation, peer, and tick fields without changing `reportVersion` `2`

## [1.1.2] - 2026-08-17

### Changed

- Session dispatch detail uses a title, Fields, and Handlers sections instead of a flat indented list

### Fixed

- Session list hover highlight now fills the full row instead of a short inset grey box

## [1.1.1] - 2026-08-17

### Changed

- Session dispatch list rows show player, a short field preview (chat text, screen, item, and similar), cancel state, and handler count so you can scan without opening each dispatch

## [1.1.0] - 2026-08-17

### Added

- NeoForge/Forge Screen reopens on the last tab, session, run, and dispatch
- Client toast `Copied` after a successful click-to-copy of an export path

### Fixed

- Export toast no longer includes the full file path, so it stays on screen; chat still has click-to-copy **Saved to** / **Folder** lines

## [1.0.0] - 2026-08-17

### Added

- Client mods: clickable `/eventlens` status, listeners, and trace start/stop/list/view/export
- Client trace events beyond tick: chat, screens, attack, use item/block/entity/empty, move, join/disconnect, respawn, pause, game mode, input, tooltip, screenshot, toast, sound, entity/chunk load, world/player tick, recipes
- Optional `eventlens-client-agent` for NeoForge and Forge per-mod handler timing (`docs/adr/0003-client-java-agent.md`)
- `/eventlens trace restart <session>` reuses the same session ID, keeps previous runs, and marks the session `RESTARTED`
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
- Forge `[Open UI]` chat clicks are handled on the client (chat mouse event, mixins, and a server `eventlens ui` fallback) so they no longer show `unknown or incomplete command`
- Forge `runClient` attaches the client agent so instrumentation can report `precise` instead of dispatch-only
- Paper plugin JAR now shades `eventlens-core`, so `/eventlens` loads on a local `runServer`
- `/eventlens` with no arguments shows status instead of an incomplete-command error
- Forge `[Open UI]` remaps the click to `/eventlensui` and intercepts the chat mouse click on the client so it does not depend on mixins applying in `runClient`
- Client export chat copies the file and folder paths on click, matching the Paper export path lines
- Client mods show toast notices for start, stop, pause, resume, restart, and export
- EventLens Screen lists open on double-click (Events starts, Sessions opens the session)
- Restart keeps earlier runs on the same session id (`--run` / Screen **Run N/M**)
