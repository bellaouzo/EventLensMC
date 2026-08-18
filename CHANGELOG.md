# Changelog

All notable changes to EventLens are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.10.3-beta] - 2026-08-18

### Added

- **Forge 26.2** and **Fabric 26.2** client mods ship with the preview release (Screen, HUD, chat commands, 55 client events)
- GitHub pre-release includes Paper, NeoForge, Forge, and Fabric client mod JARs

### Changed

- Forge client mod migrated to **EventBus 7** (`SubscribeEvent`, static `ModList`, tick record events, `ForgeLayer` HUD registration)
- Fabric client mod migrated to Fabric API **0.157+26.2** (`ClientCommands`, `KeyMappingHelper`, `HudElementRegistry`, `END_LEVEL_TICK`, play payload registries)

### Fixed

- NeoForge client mod no longer crashes on startup with `NoClassDefFoundError: AgentRuntime$OwnerIdResolver`; `eventlens-observability` is bundled in the mod JAR again
- Forge and Fabric are included in the default Gradle `check` / `build` again

## [1.10.2-beta] - 2026-08-18

### Added

- Public **preview/beta** release targeting **Paper 26.2** and **NeoForge 26.2** client mod
- GitHub pre-release ships the Paper plugin JAR and NeoForge client mod JAR

### Changed

- Paper `runServer`, `runServerDebug`, and CI smoke test auto-accept the Minecraft EULA for headless runs
- Client UI ported to Minecraft **26.2** GUI APIs (`GuiGraphicsExtractor`, `Identifier`, `gui.screen()`, etc.)
- NeoForge correlation packets send via `ServerboundCustomPayloadPacket`

### Fixed

- CI `paperSmokeTest` no longer fails when `run/eula.txt` is missing

### Known limitations (preview)

- **Forge** and **Fabric** 26.2 ports are in progress and excluded from the default Gradle `check` / `build` until they compile again
- Client Java agent remains NeoForge/Forge only; Fabric agent is still deferred
- Removed handlers for NeoForge events dropped in 26.2 (`EntityInteractSpecific`, `RecipesUpdatedEvent`)

## [1.10.1] - 2026-08-18

### Fixed

- Client `/eventlens trace export <session>` accepts `--format json|ndjson|text|html`, `--shareable`, and `--full` again (Brigadier no longer rejects tokens after the session id)
- Client export still writes a single file; `--format bundle` is Paper-only

## [1.10.0] - 2026-08-18

### Added

- Dashboard Compare lists paired client/Paper dispatches side by side when both sides have correlation keys
- Bundle `index.html` accepts `?dispatch=n` and `?view=timeline` to open a specific dispatch
- Bundle export embeds the listener-registry event graph so Event graph stays complete offline
- Client HUD shows last cancel state and `linked` when the last dispatch has a peer
- Session title `linked {peer}` jumps to that peer dispatch when the session is loaded
- Screen export toast reports `Peer found` or `No peer`; the file path still goes to chat

## [1.9.0] - 2026-08-18

### Changed

- Fabric `/eventlens ui` now uses the same Home / Events / Sessions / Session Screen as NeoForge and Forge
- Fabric HUD on/off persists in `ui.properties` and the Screen restores the last tab and session

## [1.8.0] - 2026-08-18

### Added

- Client `/eventlens trace start` accepts comma-separated events and `--preset click-flow` (use / use-block / use-entity / attack / attack-block)
- Events tab **Start click-flow** on NeoForge, Forge, and Fabric

## [1.7.0] - 2026-08-18

### Added

- `/eventlens trace start --preset protection-flow` (and `block-flow`, `inventory-flow`, `chat-flow`, `login-flow`, `command-flow`, `plugin-watch`) can omit the event name; the preset supplies the list
- First-class snapshot fields for `AsyncPlayerPreLoginEvent`, richer `PrepareItemCraftEvent` matrix/result, and `PlayerLoginEvent` kick/result
- `trace view` names both plugins when two plugins wrote cancel; `trace stop --compare-baseline <name>` prints the existing compare after stop

### Changed

- `plugin-watch` requires `--plugin` (or a preset `plugin:` value)

## [1.6.0] - 2026-08-18

### Added

- Client `/eventlens mod` and `/eventlens exceptions` on Fabric, NeoForge, and Forge
- Paper command-handler tests for `events`, `exceptions`, and `trace correlate`
- `paperSmokeTest` drives EventLensTestTarget in trace mode
- CI inspects the plugin JAR for `plugin.yml`, the main class, and shaded core classes

### Changed

- Fabric `/eventlens mod` says the list is a coarse loaded-mod view, not a `@SubscribeEvent` inventory
- `/eventlens ui` chat fallback names the Screen on all three loaders

## [1.5.0] - 2026-08-18

### Added

- First-class Paper snapshots for 95 more common events, including toggles, XP, advancements, pistons, redstone, breeding, potions, raids, weather, chunks, and anvil/enchant/brew prep
- Thirteen more client events on all loaders: heal, food, air, XP, hotbar slot, sprint/sneak/jump, glide/swim/sleep, plus screen click and key

### Fixed

- Client `/eventlens trace start` keeps suggesting `--max-events`, `--mod`, and `--player` after `--confirm-hot`
- Paper `trace start` also suggests the remaining flags after `--confirm-hot`

## [1.4.1] - 2026-08-17

### Fixed

- Event name completion and `/eventlens events <text>` now match a word anywhere in the name, so `explode` finds `EntityExplodeEvent`
- `ClientHurtEvent` records when you take fall or mob damage on the client, instead of waiting for a server-only bus event

## [1.4.0] - 2026-08-17

### Added

- First-class Paper snapshots for 24 more conflict-heavy events, including explosions, PvP damage, entity clicks, buckets, signs, crafting, fishing, portals, kicks, vehicles, and fire
- Nine more client events on NeoForge and Forge: item toss/pickup, death, hurt, use-entity-at, finish using an item, container open/close, and mining speed
- `/eventlens listeners` and `--player` / `--world` filters now resolve the acting player for inventory, sign, ignite, and vehicle events

## [1.3.2] - 2026-08-17

### Added

- Overview Recent dispatches paginates in pages of eight, with Newer and Older controls

## [1.3.1] - 2026-08-17

### Changed

- Dashboard uptime switches from milliseconds to seconds (then minutes) after 1s
- First capture of each event type is marked warmup and no longer flags warnings or top offenders
- Overview dispatches and offenders open a Timeline-style dispatch view; the Overview tab returns to the summary
- Compare is a two-sided picker with live sessions, saved reports, JSON upload, and metric deltas
- Saved reports in the sidebar are collapsed by default, limited to five, and expandable
- Context inspector is grouped and color-coded instead of a flat key-value dump
- Compare lists only `.json` reports, says so in the picker, and hides the list once both sides are selected
- Dispatch view no longer has a second Overview button; use the Overview tab to leave a dispatch

### Fixed

- Saved reports stay hidden while the sidebar section is collapsed

## [1.3.0] - 2026-08-17

### Changed

- Dashboard chrome now matches the operator mockup: top tabs, Live/Offline source toggle, session cards, saved reports, and a context inspector
- Overview, Timeline, Flame graph, Event graph, Plugin graph, and Compare views use the mockup layout and near-black / orange palette

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
