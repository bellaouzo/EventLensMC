# EventLens client UI

In-game Screen and optional HUD for the **Minecraft 1.21.1** client. NeoForge: `.\gradlew.bat :eventlens-neoforge:runClient`. Minecraft Forge: `.\gradlew.bat :eventlens-forge:runClient`. Chat commands stay available.

Fabric does not have this Screen or HUD yet.

## Open the Screen

- Keybind `key.eventlens.open` (Controls → EventLens). Unbound by default; bind it there.
- `/eventlens ui` or `/el ui`
- Status chat: `[Open UI]` (Forge opens the Screen from the click; other EventLens chat clicks stay on the client)

The world keeps running while the Screen is open (`isPauseScreen` is false) so traces still capture. The Screen is a floating diagnostic panel over the live world, not a full-screen pause menu.

## Tabs

| Tab | Actions |
|---|---|
| Home | Platform, tracing, precise vs dispatch-only, HUD toggle, jump to Events/Sessions |
| Events | Searchable event list, subscriber/overlap counts, Start (hot events confirm) |
| Sessions | Searchable list, View, Pause/Resume, Restart (stopped), Stop, Export |
| Session | Searchable dispatch list and detail (fields + agent handler rows when present) |

Hover the **live** / **paused** / **idle** and **precise** / **dispatch** pills (or the Home Tracing and Instrumentation cards) for agent protocol, EventLens version, and session counts.

Pause keeps the session and stops capture. Resume starts capturing again. Restart clones a terminal session's filters into a new session ID. Chat: `/eventlens trace pause [id]`, `/eventlens trace resume [id]`, and `/eventlens trace restart <id>`.

Flame graphs and event graphs stay in `eventlens-viewer` after you export JSON.

The Events tab lists every supported client event in `SupportedModEventTypes` (33 today). Per-frame render events (`RenderGuiEvent`, `RenderLivingEvent`, `RenderLevelStageEvent`, `ViewportEvent`, and similar) and one-shot mod-bus registration events are not trace targets.

## HUD

Off by default. Toggle on Home or the HUD keybind (`key.eventlens.hud`, also unbound by default).

Shows only while a session is active: event name, last dispatch number, duration, handler count or slowest mod. Hidden while the EventLens Screen is open. Preference is stored in `config/eventlens/ui.properties`.

The HUD reads already-captured session data. It does not start traces or instrument events.

## Chat

`/eventlens status`, `listeners`, and `trace` still work. Export paths remain copy-to-clipboard in chat.

## Fabric

Not implemented. Do not put Minecraft `Screen` types in `eventlens-mod-common`. A later Fabric addendum can register its own Screen, keybinds, and HUD against the same coordinator.
