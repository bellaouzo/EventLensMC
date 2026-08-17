# ADR 0003: Client Java agent for per-mod handler timing

## Status

Accepted

## Context

The Paper agent wraps `org.bukkit.plugin.RegisteredListener.callEvent(Event)` so EventLens can time each plugin listener and attribute cancel/state changes. The optional client mods cannot do that with public APIs: [`NeoForgeListenerRegistry`](../../eventlens-neoforge/src/main/java/dev/bellaouzo/eventlens/neoforge/NeoForgeListenerRegistry.java) only sees `@SubscribeEvent` scan data, and [`ModDispatchRecorder`](../../eventlens-mod-common/src/main/java/dev/bellaouzo/eventlens/modcommon/ModDispatchRecorder.java) marks traces `AGENT_ABSENT`.

Public NeoForge/Fabric APIs cannot list `addListener()` handlers or time another mod's callback without changing dispatch. That is the same gap ADR 0001 solved on Paper.

The existing `eventlens-agent` JAR cannot be attached to Minecraft 1.21.1: it is Java 25 / Paper 26.2 and targets `RegisteredListener`, which never exists on the client.

## Decision

Add an optional `eventlens-client-agent` artifact loaded at JVM startup with `-javaagent`. It uses ByteBuddy to wrap NeoForge and Forge game-bus listener `invoke` methods with observation-only timing. Shared `eventlens-observability` holds the registry and protocol (compiled as Java 21 bytecode so a Java 21 client can load it). Each bus has its own allowlist and fail-closed verify path.

Fabric is deferred. It has no single `callEvent` equivalent; `ArrayBackedEvent` invokers are per-event and implementation-unstable. A later ADR addendum must verify a Fabric target before any Fabric transform.

## Target

Pinned to NeoForge `21.1.244` (Event Bus `net.neoforged.bus`). Confirmed from NeoForged/Bus: `EventBus` dispatches with `listeners[index].invoke(event)`.

| Property | Value |
|---|---|
| Classes | `net.neoforged.bus.ConsumerEventHandler`, `net.neoforged.bus.ConsumerEventHandler$WithPredicate`, `net.neoforged.bus.SubscribeEventListener` |
| Method | `invoke(net.neoforged.bus.api.Event)` |
| Platform | NeoForge 1.21.1 client |
| Transform | Method entry/exit `nanoTime` plus bounded snapshots; exceptions propagate unchanged |

`GeneratedEventListener` is not transformed. `SubscribeEventListener.invoke` already calls it, so timing the wrapper once avoids double-counting.

## Fail-closed behavior

If an allowlisted class is already loaded and `invoke(Event)` is missing, the agent logs an error and does not install transforms. If the class is not loaded yet, the transform is installed by name and applies when NeoForge loads the bus. The client mod continues in dispatch-only mode.

## Observation integrity

The advice must not cancel events, reorder listeners, swallow exceptions, or invoke listeners twice. `ObservationGate` stays off unless a client trace session is active. Hot events still require `--confirm-hot` and a low max-events cap. Stack capture is opt-in and only on slow-handler exit.

Bounded snapshots use the same field/string/depth limits as the Paper plugin.

## Consequences

- Operators must add `-javaagent:eventlens-client-agent.jar` to the **client** JVM. NeoForge and Forge `runClient` attach it automatically.
- `eventlens-observability` is Java 21 bytecode (still built with the Java 25 toolchain).
- `ListenerObservation.pluginName` is the **mod id** on the client.
- Lambda handlers may appear as `<lambda>`.
- Mixins, raw LWJGL, and vanilla callbacks that skip the NeoForge game bus stay invisible.
- NeoForge bus minors require signature re-verification.

## Alternatives considered

- **Scan-only** — already implemented; cannot time `addListener()` or attribute cancel.
- **Mixin wrap of other mods** — changes load order and violates observation integrity.
- **One shared agent JAR** — mixes Java 25 Paper verify with Java 21 client targets; fail-closed paths become easy to get wrong.
- **Instrument `EventListener.invoke` on the sealed abstract type** — subclasses override `invoke`, so the abstract method is never the call site.

## Rollback

Remove `-javaagent` from the client JVM arguments. The client mod degrades to dispatch-level records plus `@SubscribeEvent` scan overlap.

## Addendum: Forge Event Bus 6.2.33 (Forge 52.1.16)

Verified from `net.minecraftforge:eventbus:6.2.33` shipped with Forge 52.1.16. Forge does **not** have NeoForge's `ConsumerEventHandler` / `SubscribeEventListener` classes.

`EventBus.post` dispatches with `IEventBusInvokeDispatcher.invoke(listener, event)`, and each listener implements `IEventListener.invoke(Event)`.

| Property | Value |
|---|---|
| Classes | `net.minecraftforge.eventbus.ASMEventHandler` |
| Method | `invoke(net.minecraftforge.eventbus.api.Event)` |
| Platform | Minecraft Forge 1.21.1 (`52.1.16`) client |
| Extra matcher | `net.minecraftforge.eventbus.EventBus$$Lambda*` for `addListener(Consumer)` wrappers |

`NamedEventListener` is a debug wrapper (`eventbus.namelisteners`) that forwards to another listener. It is not transformed, to avoid double-counting.

`ASMEventHandler.invoke` already calls the generated handler, so the generated class is not transformed.

Cancel state is read from `net.minecraftforge.eventbus.api.Event.isCanceled()`. There is no `ICancellableEvent` on this bus version.

If Forge signatures mismatch, Forge transforms are skipped and NeoForge transforms can still install. The client mod continues in dispatch-only mode when neither bus installs.

## Fabric

Not implemented. Do not share NeoForge or Forge transforms with Fabric. Revisit only after a Fabric invoker target is verified.
