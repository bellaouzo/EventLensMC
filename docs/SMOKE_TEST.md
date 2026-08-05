# EventLens Smoke Testing

EventLens has two smoke paths:

- Automated headless smoke via `paperSmokeTest`
- Manual in-console and in-game validation

## Automated smoke (headless Paper)

Run:

```powershell
.\gradlew.bat paperSmokeTest --warning-mode all
```

What this task does:

1. Starts Paper with `runServer` in headless mode.
2. Waits for server readiness in `run/logs/latest.log`.
3. Runs EventLens console commands:
   - `eventlens status`
   - `eventlens listeners BlockBreakEvent`
   - `eventlens trace list`
   - `eventlens trace start BlockBreakEvent --max-events 5 --max-duration 10s`
   - `eventlens trace stop`
   - `stop`
4. Asserts required log markers, including startup and clean shutdown.

Use this task for CI and local pre-PR checks.

## CI smoke job

GitHub Actions runs `paperSmokeTest` in the `smoke` job on `windows-latest` after `check` passes.

## Manual smoke checklist

Run:

```powershell
.\scripts\smoke-test.ps1
```

Or, full automation plus follow-up instructions:

```powershell
.\scripts\smoke-test-full.ps1
```

After the server prints `Done`, validate:

```text
eventlens status
eventlens listeners PlayerJoinEvent
eventlens listeners BlockBreakEvent
eventlens trace start BlockBreakEvent --max-events 50 --max-duration 60s
eventlens trace list
eventlens trace view <sessionId> --unchanged
eventlens trace stop
```

Expected outcomes:

- status includes version/platform/tracing state
- listeners output includes plugin, listener owner, priority, and order
- unsupported trace event types are rejected
- supported trace event types start successfully

Stop with:

```text
stop
```

Never use `/reload`.
