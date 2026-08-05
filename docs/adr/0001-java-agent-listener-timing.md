# ADR 0001: Java agent for per-listener timing

## Status

Accepted

## Context

EventLens must measure each registered listener's execution time during an event dispatch. Paper/Bukkit priority-band checkpoints only approximate where time is spent. The public API exposes `org.bukkit.plugin.RegisteredListener.callEvent(Event)`, which is invoked once per listener and is a stable observation point.

## Decision

Add an optional `eventlens-agent` artifact loaded at JVM startup with `-javaagent`. The agent uses ByteBuddy to wrap `RegisteredListener.callEvent(Event)` with observation-only timing code. A shared `eventlens-observability` module holds the in-memory registry and protocol version used by both agent and plugin.

## Target

| Property | Value |
|---|---|
| Class | `org.bukkit.plugin.RegisteredListener` |
| Method | `callEvent(org.bukkit.event.Event)` |
| Platform | Paper 26.2 |
| Transform | Method entry/exit `nanoTime` only; exceptions propagate unchanged |

## Fail-closed behavior

If the target method signature does not match at agent startup, the agent logs an error and does not install transforms. The plugin continues in dispatch-only timing mode.

## Observation integrity

The advice must not cancel events, reorder listeners, swallow exceptions, or invoke listeners twice. Stack capture is opt-in and only on slow-listener exit when `--capture-stacks` is enabled.

## Consequences

- Operators must add `-javaagent:eventlens-agent.jar` to the server JVM for per-listener metrics.
- Agent JAR must be built for the same protocol version as the plugin.
- Lambda listener method names may appear as `<lambda>`.
- Future Paper versions require signature re-verification.

## Rollback

Remove `-javaagent` from JVM arguments. The plugin degrades to dispatch-level timing without per-listener rankings.
