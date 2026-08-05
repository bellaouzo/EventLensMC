# Debugging Plugins With EventLens

This guide covers repeatable workflows for debugging plugin interactions on a local Paper server with EventLens enabled.

## Prerequisites

- Java 25 and `JAVA_HOME` set
- EventLens repository checkout
- PowerShell (Windows-first workflow)

## Start a debug server

Run Paper with debug attach and EventLens Java agent enabled:

```powershell
.\gradlew.bat runServerDebug
```

Attach your IDE to `127.0.0.1:5005` once Paper finishes startup.

## Baseline checks

From the server console:

```text
eventlens status
eventlens listeners BlockBreakEvent
eventlens listeners PlayerInteractEvent
```

Expected indicators:

- `status` reports EventLens version, platform, and instrumentation mode
- `listeners` prints plugin/listener ordering for the requested event

## Targeted trace workflow

1. Start a bounded trace:

   ```text
   eventlens trace start BlockBreakEvent --max-events 128 --max-duration 60s --slow-threshold 1ms
   ```

2. Reproduce the behavior in-game.
3. Inspect captured output:

   ```text
   eventlens trace list
   eventlens trace view <sessionId> --detail verbose --unchanged
   ```

4. Stop trace session(s):

   ```text
   eventlens trace stop
   ```

## Investigating command/permission issues

Validate command permissions declared in `plugin.yml` and by handler checks:

- `eventlens.command.status`
- `eventlens.command.listeners`
- `eventlens.command.plugin`
- `eventlens.command.trace` (+ child nodes)

Use a non-op test account to confirm permission-denied behavior, then grant nodes selectively.

## Common log markers

Check `run/logs/latest.log` for:

- `EventLens v... enabled for Paper 26.2`
- `EventLens agent attached` or fallback warning
- `EventLens disabled.`

If startup fails due to stale lock/log handles, stop stale processes and clean locks:

```powershell
.\scripts\stop-server.ps1 -Force
```

## Stop cleanly

Always stop Paper with:

```text
stop
```

Do not use `/reload`.
