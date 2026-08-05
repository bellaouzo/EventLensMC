# EventLens

EventLens is a Paper server plugin that helps diagnose how Minecraft events flow through registered plugin listeners.

## Requirements

- Java 25 JDK
- Windows PowerShell (or any shell that can run `gradlew.bat`)

Set `JAVA_HOME` to your Java 25 installation. The project uses the Gradle Wrapper; a global Gradle install is not required.

## Quick start

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat runServer
```

On first server start, accept the Minecraft EULA in `run/eula.txt` when prompted.

## Status command

```
/eventlens status
```

Permission: `eventlens.command.status` (default: op)

## Documentation

- [Setup guide](docs/SETUP.md)
- [Architecture overview](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Setup session log](docs/setup-session.md)

## License

See repository license terms when added.
