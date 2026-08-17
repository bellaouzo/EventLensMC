# EventLens client Java agent

Optional `-javaagent` for the **Minecraft 1.21.1** NeoForge and Forge clients. It times other mods' game-bus handlers the way the Paper agent times `RegisteredListener.callEvent`.

The client mod stays useful without it: dispatch records and `@SubscribeEvent` overlap still work.

## What it does

When a client trace session is active, the agent observes each `invoke(Event)` on:

NeoForge (`net.neoforged.bus`):

- `ConsumerEventHandler` (`addListener`)
- `ConsumerEventHandler$WithPredicate`
- `SubscribeEventListener` (`@SubscribeEvent`)

Forge Event Bus 6.2.33 (`net.minecraftforge.eventbus`):

- `ASMEventHandler` (`@SubscribeEvent`)
- `EventBus$$Lambda*` (`addListener` consumer wrappers)

It records duration, owning mod id when recoverable, and a bounded before/after snapshot (cancel plus a few event fields). Exceptions still propagate.

## What it does not do

- Fabric (deferred; no verified invoker target yet). Fabric `runClient` does not attach this agent.
- Mixins, raw input, or vanilla callbacks that skip the game bus
- Change, cancel, reorder, or double-invoke handlers
- Live sync with the Paper server

## Dev attach

NeoForge and Forge `runClient` build the agent JAR and add `-javaagent` automatically:

```
.\gradlew.bat :eventlens-neoforge:runClient
.\gradlew.bat :eventlens-forge:runClient
```

## Real launcher

Add a JVM argument (same machine, next to the observability JAR the agent stages beside itself):

```
-javaagent:eventlens-client-agent-1.0.0.jar
```

Place `eventlens-observability-1.0.0.jar` in the same folder as the agent JAR.

## Status in chat

`/eventlens` shows `Instrumentation  precise` when the agent loaded, or `dispatch-only` when it did not.

## Tests

Forked JVM load test: `:eventlens-client-agent:test`

Manual: start `ClientChatEvent`, send chat, `trace view` should list handler rows (EventLens plus any other chat mods) with times. Quit with **Quit Game**.

See `docs/adr/0003-client-java-agent.md`.
