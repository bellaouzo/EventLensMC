# EventLens Setup

This guide covers local development setup for EventLens on Windows with Cursor.

## Prerequisites

- Java 25 JDK (Eclipse Temurin recommended)
- Git
- Cursor with the Java Extension Pack

Ensure `JAVA_HOME` points to Java 25 and `%JAVA_HOME%\bin` is on your user `Path`.

Verify:

```powershell
java -version
javac -version
.\gradlew.bat --version
```

Expected:

- Java and Javac report version 25
- Gradle reports 9.6.1 with JVM 25

## Build and test

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
```

The plugin JAR is written to `build/libs/EventLens-1.0-SNAPSHOT.jar`.

## Run the Paper test server

```powershell
.\gradlew.bat runServer
```

The run-paper plugin downloads Paper 26.2 into `run/` and loads the built plugin automatically.

### EULA

If the server stops because `run/eula.txt` contains `eula=false`, edit the file and set:

```
eula=true
```

Only do this after you accept the [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula).

Do not use `/reload` while developing EventLens. Restart the server after code changes.

## Debugging workflow

1. Start the debug server task:

   ```powershell
   .\gradlew.bat runServerDebug
   ```

   This binds the JVM debugger to `127.0.0.1:5005` without suspending startup.

2. In Cursor, run the launch configuration **EventLens: Attach to Paper Server**.

3. Set breakpoints in EventLens source files.

4. Trigger behavior in-game or through the server console.

5. Stop the server with the `stop` console command when finished.

## Cursor tasks

Available from **Terminal → Run Task**:

- EventLens: Clean Build (default build task)
- EventLens: Build
- EventLens: Test
- EventLens: Run Paper Server
- EventLens: Run Paper Server (Debug)
- EventLens: Stop Gradle Daemons

## Recommended extensions

- `vscjava.vscode-java-pack`
- `redhat.vscode-yaml`
- `usernamehw.errorlens` (optional)
- `eamodio.gitlens` (optional)

If an extension is listed in `.vscode/extensions.json` but not installed, use Cursor's extension panel to install it.

## Troubleshooting

- If Gradle uses the wrong Java version, run `.\gradlew.bat --stop` and confirm `JAVA_HOME`.
- If the server fails to bind a port, stop any previous `runServer` process and delete stale lock files under `run/`.
- If plugin changes are not picked up, run `.\gradlew.bat clean build` and restart the server.
