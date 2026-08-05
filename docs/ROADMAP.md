# EventLens Roadmap

## Milestone 1 — Environment and skeleton (current)

- [x] Gradle Wrapper 9.6.1 on Java 25
- [x] Paper 26.2 compile and test server integration
- [x] Plugin bootstrap with status command
- [x] Cursor project rules and documentation

## Milestone 2 — Trace session lifecycle

- [ ] Start/stop trace session commands
- [ ] Bounded in-memory trace buffers
- [ ] Session listing and timeout handling
- [ ] Clean disable on plugin shutdown

## Milestone 3 — Listener discovery

- [ ] Resolve registered listeners for a target event type
- [ ] Show plugin owner, priority, and execution order
- [ ] Cache safe metadata outside hot paths

## Milestone 4 — Event snapshots

- [x] Initial supported event allowlist (`SupportedEventTypes`)
- [x] Capture supported event properties before and after listener execution (checkpoints)
- [x] Record cancellation changes
- [ ] Expand supported events beyond the initial ten

## Milestone 5 — Performance tracing

Design defaults live in `docs/PERFORMANCE.md`. Measurement, ranking, and self-throttling are implemented.

### Dispatch timing

- [x] Measure total event-dispatch duration accurately (nanosecond clock)
- [x] Show min / max / average dispatch duration per session
- [x] Show p50, p95, and p99 dispatch duration per session

### Per-listener timing

- [x] Measure each listener’s execution time (Java agent)
- [x] Rank slowest listeners (by p95 and max)
- [x] Rank slowest plugins (aggregate listener timings)
- [x] Highlight listeners exceeding a configurable threshold (`--slow-threshold`)
- [x] Show average listener duration per listener identity
- [x] Count how often each listener runs during a session
- [x] Detect unusually frequent listeners (rate vs session baseline)
- [x] Optional stack trace capture for slow listeners (`--capture-stacks`, admin opt-in)

### Main thread and overhead

- [x] Detect listeners blocking the main thread (sync dispatch, >= 5 ms)
- [x] Automatic throttling when EventLens overhead exceeds budget
- [x] Sampling for extremely frequent events (`PlayerMoveEvent` 1:20 plus filter)
- [x] Mark traces partial when throttled, sampled, or truncated

### Output

- [x] Include timing summary in `trace view`
- [x] Include slow-listener and slow-plugin rankings in session summary
- [ ] Export timing aggregates (Milestone 6)

## Milestone 6 — Filtering and export

- [ ] Filter by event, plugin, player, world, or region
- [ ] Console and file export formats
- [ ] Optional structured JSON export for tooling

## Milestone 7 — Quality gates and smoke automation

- [x] Add MockBukkit dependency for Paper 26.x branch
- [x] Add basic command permission tests with MockBukkit
- [x] Add `paperSmokeTest` Gradle verification task
- [x] Add CI smoke job that runs headless Paper startup and command checks

## Milestone 8 — Operational documentation

- [x] Add plugin debugging runbook (`docs/DEBUGGING_PLUGINS.md`)
- [x] Add smoke test runbook (`docs/SMOKE_TEST.md`)
- [x] Add trace report schema (`docs/schemas/trace-report-v2.schema.json`)
- [x] Add full smoke helper script (`scripts/smoke-test-full.ps1`)

## Non-goals (for now)

- Client-side mods
- NMS-heavy instrumentation
- Always-on global tracing
- Modifying other plugins' listener behavior
