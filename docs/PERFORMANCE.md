# EventLens Performance

Performance limits are **EventLens design defaults**. Minecraft targets ~20 TPS (~50 ms per tick); EventLens must consume only a small fraction of that and degrade automatically under load.

## Resource budgets (initial defaults)

| Resource | Default | Hard behavior |
|---|---|---|
| Concurrent sessions | 4 | Reject fifth with reason |
| Records per session | 4,096 | Ring buffer / stop per policy |
| Listener records per dispatch | 256 | Truncate remainder |
| Snapshot fields | 64 | Stop capture, mark truncated |
| Object depth | 2 | Replace deeper values with marker |
| Collection/map entries | 32 | Capture bounded prefix + omitted count |
| Captured string | 512 chars | Truncate with length metadata |
| Serialized record | 16 KiB | Drop optional fields, then reject if still oversized |
| Export file | 32 MiB | Stop export, report incomplete |
| Pending exports | 2 | Reject or coalesce |

## Sampling

| Event class | Default |
|---|---|
| Normal | 1:1 (subject to budgets) |
| Hot (e.g. movement) | 1:20 plus an additional filter |

Hot-event tracing requires at least one narrowing filter (player, plugin, world, region). `--cancelled` alone is not sufficient.

## Overhead targets (per observed dispatch)

| Measurement | Target / action |
|---|---|
| Average | <= 0.25 ms |
| p95 | <= 0.75 ms |
| Throttle | > 1 ms p95 over 200 samples |
| Auto-stop | > 2.5 ms p95 for three consecutive windows |
| Emergency stop | Any single EventLens operation > 10 ms |

Throttling marks sessions `THROTTLED`, skips intermediate snapshot checkpoints, and records partial reasons on dispatches.

## Threading and hot paths

On the main thread, EventLens may only perform bounded synchronous capture.

Forbidden in hot paths: disk I/O, network I/O, large JSON, blocking locks, recursive reflection, sleep.

Expensive work runs asynchronously from immutable DTOs.

## Benchmarking policy

Do not optimize hot paths without before/after measurement. JMH may be added later for snapshot/diff microbenchmarks.

## Current status

**Implemented**

- Nanosecond dispatch duration (`nanoTime` LOWEST→MONITOR) on `TraceDispatchRecord.durationNanos`
- Per-listener execution timing via optional `eventlens-agent` (`RegisteredListener.callEvent` instrumentation)
- Session dispatch stats: min, max, average, p50, p95, p99
- Per-listener and per-plugin slow rankings in `trace view`
- Slow-listener highlighting via `--slow-threshold` (default 1 ms)
- Main-thread blocking detection for sync dispatches (>= 5 ms listener duration)
- Optional stack capture via `--capture-stacks` (agent path, slow-listener exit)
- Listener run counts and unusually frequent listener warnings (3× median rate)
- EventLens self-overhead measurement and `PerformanceBudgetController` auto-throttle/auto-stop
- Hot-event sampling enforcement for `PlayerMoveEvent` (1:20 + narrowing filter)
- Partial trace markers (`SAMPLED`, `THROTTLED`, `RECORD_LIMIT`, `LISTENER_LIMIT`, `AGENT_ABSENT`)

**Limitations**

- Agent targets Paper 26.2 `RegisteredListener` signature; load with `-javaagent:eventlens-agent.jar` at JVM startup
- Without the agent, dispatch-level timing works; per-listener rankings show `AGENT_ABSENT`
- Lambda listener method names may appear as `<lambda>`
- Async events (e.g. `AsyncChatEvent`) are timed but not flagged as main-thread blockers
