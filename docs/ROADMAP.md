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

- [ ] Capture supported event properties before listener execution
- [ ] Capture properties after listener execution
- [ ] Record cancellation changes

## Milestone 5 — Timing and warnings

- [ ] Measure per-listener execution time
- [ ] Flag slow listeners with configurable thresholds
- [ ] Summarize totals per trace session

## Milestone 6 — Filtering and export

- [ ] Filter by event, plugin, player, world, or region
- [ ] Console and file export formats
- [ ] Optional structured JSON export for tooling

## Non-goals (for now)

- Client-side mods
- NMS-heavy instrumentation
- Always-on global tracing
- Modifying other plugins' listener behavior
