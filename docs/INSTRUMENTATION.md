# EventLens Instrumentation

## Policy summary

1. **Supported Paper APIs first**
2. **Reflection / NMS only with an ADR**
3. **Java agent as optional separate artifact**

The standard plugin must remain useful without an agent.

## Supported-API path (current and preferred)

Initial milestones should use:

- Registered listener metadata available through Paper/Bukkit APIs
- Explicit snapshot adapters for supported event types
- Main-thread bounded capture + async export

## Java agent (implemented)

Gradle subproject: `eventlens-agent`

- Load with `-javaagent` at JVM startup (`runServer` / `runServerDebug` attach automatically in dev)
- Transforms `org.bukkit.plugin.RegisteredListener.callEvent(Event)` with observation-only timing
- Shares protocol and registry with plugin via `eventlens-observability`
- Fail closed if target method signature mismatches (Paper 26.2 assumption)
- See `docs/adr/0001-java-agent-listener-timing.md`

Build output: `eventlens-agent/build/libs/eventlens-agent-0.1-SNAPSHOT.jar`

## Instrumentation SPI

Plugin-side port: `InstrumentationPort` with `AgentInstrumentationAdapter` and `NoOpInstrumentationAdapter`.

Domain code depends on the port, not agent classes.

## When reflection or NMS might be considered

Requires `docs/adr/` record covering:

- Missing capability and why public APIs are insufficient
- Exact version assumptions
- Failure and rollback behavior
- Test plan on real Paper

Do not add `paperweight-userdev` without an approved ADR.

## Current status

- `eventlens-observability` shared registry and protocol version 2 (minimum supported: 1)
- `eventlens-agent` ByteBuddy premain with forked-JVM load test
- Per-listener before/after snapshots when agent is attached and the plugin registers `ListenerSnapshotBridge`
- Plugin-only fallback: priority-band checkpoints (LOWEST..MONITOR) with band-level property attribution
- `/eventlens status` reports instrumentation mode (precise / dispatch-only / degraded), capabilities, and diagnostic lines
- Trace detail output includes per-listener property changes and cancellation timeline when snapshots are available
- Listener exceptions propagate unchanged; EventLens records exception type and timing only
- No reflection-based tracing in the plugin path, no NMS usage

See also `.cursor/rules/instrumentation.mdc`.
