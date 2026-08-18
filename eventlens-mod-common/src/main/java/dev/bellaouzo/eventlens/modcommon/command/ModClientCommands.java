package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatColor;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class ModClientCommands {

    private ModClientCommands() {}

    public static List<ModChatLine> execute(ModTraceCoordinator coordinator, String ownerName, List<String> args) {
        if (args.isEmpty() || "status".equalsIgnoreCase(args.getFirst())) {
            return ModStatusFormatter.render(coordinator.status());
        }
        return switch (args.getFirst().toLowerCase(Locale.ROOT)) {
            case "listeners" -> listeners(coordinator, args);
            case "trace" -> trace(coordinator, ownerName, args);
            case "mod" -> ModProfileCommands.handle(coordinator, args);
            case "exceptions" -> ModProfileCommands.exceptions(coordinator);
            case "ui" -> ModTraceFormatter.uiUnavailable();
            default -> ModTraceFormatter.usage();
        };
    }

    private static List<ModChatLine> listeners(ModTraceCoordinator coordinator, List<String> args) {
        String event = args.size() >= 2 ? args.get(1) : "";
        List<ModHandlerRegistration> handlers = List.of();
        if (!event.isBlank()) {
            String className = SupportedModEventTypes.resolveClassName(event);
            if (className != null) {
                handlers = coordinator.listenerRegistryPort().listHandlers(className);
            }
        }
        return ModListenersFormatter.listeners(event, handlers);
    }

    private static List<ModChatLine> trace(ModTraceCoordinator coordinator, String ownerName, List<String> args) {
        if (args.size() < 2) {
            return ModTraceFormatter.traceUsage();
        }
        return switch (args.get(1).toLowerCase(Locale.ROOT)) {
            case "start" -> start(coordinator, ownerName, args);
            case "stop" -> stop(coordinator, ownerName, args);
            case "pause" -> pause(coordinator, ownerName, args);
            case "resume" -> resume(coordinator, ownerName, args);
            case "restart" -> restart(coordinator, args);
            case "list" -> ModTraceFormatter.list(coordinator.listSessions());
            case "view" -> view(coordinator, args);
            case "export" -> export(coordinator, args);
            case "live" -> List.of(ModChatLine.text(
                    "Live alerts use the HUD and toasts on this client.",
                    ModChatColor.YELLOW));
            default -> ModTraceFormatter.traceUsage();
        };
    }

    private static List<ModChatLine> start(ModTraceCoordinator coordinator, String ownerName, List<String> args) {
        if (args.size() < 3) {
            return ModTraceFormatter.traceUsage();
        }
        boolean confirmHot = false;
        Optional<Integer> maxEvents = Optional.empty();
        var filter = dev.bellaouzo.eventlens.domain.trace.TraceFilter.Builder.unrestricted();
        for (int i = 3; i < args.size(); i++) {
            String token = args.get(i);
            if ("--confirm-hot".equalsIgnoreCase(token)) {
                confirmHot = true;
            } else if ("--max-events".equalsIgnoreCase(token) && i + 1 < args.size()) {
                try {
                    maxEvents = Optional.of(Integer.parseInt(args.get(++i)));
                } catch (NumberFormatException ignored) {
                    return List.of(ModChatLine.text("--max-events requires a number.", ModChatColor.RED));
                }
            } else if (("--mod".equalsIgnoreCase(token) || "--player".equalsIgnoreCase(token))
                    && i + 1 < args.size()) {
                String value = args.get(++i);
                if ("--mod".equalsIgnoreCase(token)) {
                    filter.pluginName(value);
                } else {
                    filter.playerName(value);
                }
            }
        }
        coordinator.setStartFilter(filter.build());
        return ModTraceFormatter.start(coordinator.startTrace(args.get(2), ownerName, confirmHot, maxEvents));
    }

    private static List<ModChatLine> stop(ModTraceCoordinator coordinator, String ownerName, List<String> args) {
        if (args.size() >= 3) {
            return ModTraceFormatter.stop(coordinator.stopSession(args.get(2)));
        }
        return ModTraceFormatter.stop(coordinator.stopTraces(ownerName));
    }

    private static List<ModChatLine> pause(ModTraceCoordinator coordinator, String ownerName, List<String> args) {
        if (args.size() >= 3) {
            return ModTraceFormatter.pause(coordinator.pauseSession(args.get(2)));
        }
        return ModTraceFormatter.pause(coordinator.pauseTraces(ownerName));
    }

    private static List<ModChatLine> resume(ModTraceCoordinator coordinator, String ownerName, List<String> args) {
        if (args.size() >= 3) {
            return ModTraceFormatter.pause(coordinator.resumeSession(args.get(2)));
        }
        return ModTraceFormatter.pause(coordinator.resumeTraces(ownerName));
    }

    private static List<ModChatLine> restart(ModTraceCoordinator coordinator, List<String> args) {
        if (args.size() < 3) {
            return List.of(ModChatLine.text("Usage: /eventlens trace restart <session>", ModChatColor.YELLOW));
        }
        return ModTraceFormatter.restart(coordinator.restartSession(args.get(2)));
    }

    private static List<ModChatLine> view(ModTraceCoordinator coordinator, List<String> args) {
        if (args.size() < 3) {
            return ModTraceFormatter.traceUsage();
        }
        int page = 1;
        Integer dispatch = null;
        Optional<Integer> generation = Optional.empty();
        for (int i = 3; i < args.size(); i++) {
            String token = args.get(i);
            if ("--dispatch".equalsIgnoreCase(token) && i + 1 < args.size()) {
                try {
                    dispatch = Integer.parseInt(args.get(++i));
                } catch (NumberFormatException ignored) {
                    return List.of(ModChatLine.text("--dispatch requires a number.", ModChatColor.RED));
                }
            } else if ("--run".equalsIgnoreCase(token) && i + 1 < args.size()) {
                try {
                    int run = Integer.parseInt(args.get(++i));
                    if (run < 1) {
                        return List.of(ModChatLine.text("--run must be a positive integer.", ModChatColor.RED));
                    }
                    generation = Optional.of(run - 1);
                } catch (NumberFormatException ignored) {
                    return List.of(ModChatLine.text("--run requires a number.", ModChatColor.RED));
                }
            } else {
                try {
                    page = Integer.parseInt(token);
                } catch (NumberFormatException ignored) {
                    return List.of(ModChatLine.text("Page must be a number.", ModChatColor.RED));
                }
            }
        }
        var result = dispatch != null
                ? coordinator.viewDispatch(args.get(2), dispatch, generation)
                : coordinator.viewSession(args.get(2), page, generation);
        List<ModHandlerRegistration> handlers = List.of();
        if (result.summary() != null) {
            handlers = coordinator.listenerRegistryPort().listHandlers(result.summary().eventClassName());
        }
        return ModTraceViewFormatter.view(result, handlers);
    }

    private static List<ModChatLine> export(ModTraceCoordinator coordinator, List<String> args) {
        String sessionId = args.size() >= 3 ? args.get(2) : "";
        return ModTraceFormatter.export(coordinator.exportSession(sessionId, Optional.empty()));
    }
}
