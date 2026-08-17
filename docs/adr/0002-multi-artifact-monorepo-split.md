# ADR 0002: Multi-artifact monorepo split

## Status

Accepted

## Context

EventLens started as a single Paper plugin. We want an optional client mod (NeoForge/Forge/Fabric) that shares trace/report logic with the server plugin, without coupling loaders or breaking Paper-first development.

## Decision

Split the repository into Gradle subprojects:

| Subproject | Role |
|---|---|
| `eventlens-core` | Pure Java domain, application services, trace runtime |
| `eventlens-paper` | Paper plugin, commands, Paper adapters |
| `eventlens-mod-common` | Client-only orchestration shared by loader adapters |
| `eventlens-neoforge` | NeoForge 1.21.1 client mod (primary client artifact) |
| `eventlens-forge` | Minecraft Forge 1.21.1 client mod (`:eventlens-forge:runClient`) |
| `eventlens-fabric` | Fabric client mod |
| `eventlens-agent` / `eventlens-observability` | Server-only Java agent (unchanged) |
| `eventlens-viewer` | Shared dashboard / offline report viewer |

- `eventlens-core` compiles with Java 21 bytecode for client compatibility; `eventlens-paper` remains Java 25.
- Cross-runtime linking is export-format + viewer only (no live server↔client sync in MVP).
- Client mods use synthetic event ids under `dev.bellaouzo.eventlens.runtime.*` (tick, chat, screens, combat, use, move, join/disconnect, input, world load, sound, tooltip).

## Consequences

- Paper plugin JAR path: `eventlens-paper/build/libs/EventLens-*.jar`
- Root `gradlew check` aggregates all subprojects.
- Client mods do not bundle the Java agent or observability protocol.
- Listener bus introspection on client is limited in MVP; exports may mark traces partial.
