# EventLens

EventLens is a Paper 26.2 server-side plugin for diagnosing how Minecraft server events travel through registered plugin listeners.

## Current stack

- Java 25 (Eclipse Temurin)
- Gradle Wrapper 9.6.1 (Groovy DSL)
- Paper API 26.2
- run-paper 3.0.2

## Commands

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat runServer
.\gradlew.bat runServerDebug
```

## Main entry point

- Package: `dev.bellaouzo.eventlens`
- Main class: `dev.bellaouzo.eventlens.EventLens`

## Architecture expectations

Keep commands, trace sessions, listener discovery, snapshots, timing, filtering, formatting/export, and Paper integration in separate packages. Use interfaces for version-sensitive or reflective behavior.

## Current implementation status

- Plugin loads on Paper 26.2
- `/eventlens status` reports version, platform, tracing flag, and active session count
- Tracing engine, listener discovery, and export are not implemented yet

## Next development milestone

Implement trace session lifecycle and listener discovery for a single filtered event type, with bounded in-memory buffers and no global always-on tracing.
