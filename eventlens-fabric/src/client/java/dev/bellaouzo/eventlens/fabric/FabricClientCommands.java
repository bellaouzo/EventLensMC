package dev.bellaouzo.eventlens.fabric;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.command.ModClientCommands;
import dev.bellaouzo.eventlens.modcommon.command.ModClientTabCompleter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;

final class FabricClientCommands {

    private FabricClientCommands() {}

    static void register(ModTraceCoordinator coordinator) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(root(coordinator, "eventlens"));
            dispatcher.register(root(coordinator, "el"));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> root(
            ModTraceCoordinator coordinator, String name) {
        return ClientCommands.literal(name)
                .executes(context -> run(coordinator, context, List.of("status")))
                .then(ClientCommands.literal("status")
                        .executes(context -> run(coordinator, context, List.of("status"))))
                .then(listeners(coordinator))
                .then(mod(coordinator))
                .then(exceptions(coordinator))
                .then(trace(coordinator))
                .then(ClientCommands.literal("ui")
                        .executes(context -> {
                            net.minecraft.client.Minecraft.getInstance()
                                    .execute(() -> dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen.open());
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> mod(ModTraceCoordinator coordinator) {
        return ClientCommands.literal("mod")
                .executes(context -> run(coordinator, context, List.of("mod")))
                .then(ClientCommands.literal("compare")
                        .then(ClientCommands.argument("left", StringArgumentType.word())
                                .suggests(suggestMods(coordinator))
                                .then(ClientCommands.argument("right", StringArgumentType.word())
                                        .suggests(suggestMods(coordinator))
                                        .executes(context -> run(
                                                coordinator,
                                                context,
                                                List.of(
                                                        "mod",
                                                        "compare",
                                                        StringArgumentType.getString(context, "left"),
                                                        StringArgumentType.getString(context, "right")))))))
                .then(ClientCommands.argument("id", StringArgumentType.word())
                        .suggests(suggestMods(coordinator))
                        .executes(context -> run(
                                coordinator, context, List.of("mod", StringArgumentType.getString(context, "id")))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> exceptions(ModTraceCoordinator coordinator) {
        return ClientCommands.literal("exceptions")
                .executes(context -> run(coordinator, context, List.of("exceptions")))
                .then(ClientCommands.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> run(
                                coordinator,
                                context,
                                List.of(
                                        "exceptions",
                                        Integer.toString(IntegerArgumentType.getInteger(context, "page"))))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> listeners(ModTraceCoordinator coordinator) {
        return ClientCommands.literal("listeners")
                .executes(context -> run(coordinator, context, List.of("listeners")))
                .then(ClientCommands.argument("event", StringArgumentType.word())
                        .suggests(suggestEvents())
                        .executes(context -> run(
                                coordinator,
                                context,
                                List.of("listeners", StringArgumentType.getString(context, "event")))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> trace(ModTraceCoordinator coordinator) {
        return ClientCommands.literal("trace")
                .executes(context -> run(coordinator, context, List.of("trace")))
                .then(start(coordinator))
                .then(sessionCommand(coordinator, "stop"))
                .then(sessionCommand(coordinator, "pause"))
                .then(sessionCommand(coordinator, "resume"))
                .then(restart(coordinator))
                .then(ClientCommands.literal("list")
                        .executes(context -> run(coordinator, context, List.of("trace", "list"))))
                .then(view(coordinator))
                .then(sessionCommand(coordinator, "export"));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> start(ModTraceCoordinator coordinator) {
        return ClientCommands.literal("start")
                .then(ClientCommands.argument("event", StringArgumentType.word())
                        .suggests(suggestEvents())
                        .executes(context -> run(
                                coordinator,
                                context,
                                List.of("trace", "start", StringArgumentType.getString(context, "event"))))
                        .then(ClientCommands.argument("flags", StringArgumentType.greedyString())
                                .suggests(suggestStartFlags(coordinator))
                                .executes(context -> {
                                    List<String> args = new ArrayList<>();
                                    args.add("trace");
                                    args.add("start");
                                    args.add(StringArgumentType.getString(context, "event"));
                                    args.addAll(List.of(StringArgumentType.getString(context, "flags").split("\\s+")));
                                    return run(coordinator, context, args);
                                })));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> view(ModTraceCoordinator coordinator) {
        return ClientCommands.literal("view")
                .then(ClientCommands.argument("session", StringArgumentType.word())
                        .suggests(suggestSessions(coordinator))
                        .executes(context -> run(
                                coordinator,
                                context,
                                List.of("trace", "view", StringArgumentType.getString(context, "session"))))
                        .then(ClientCommands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(
                                        coordinator,
                                        context,
                                        List.of(
                                                "trace",
                                                "view",
                                                StringArgumentType.getString(context, "session"),
                                                Integer.toString(
                                                        IntegerArgumentType.getInteger(context, "page")))))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> restart(ModTraceCoordinator coordinator) {
        return ClientCommands.literal("restart")
                .then(ClientCommands.argument("session", StringArgumentType.word())
                        .suggests(suggestSessions(coordinator))
                        .executes(context -> run(
                                coordinator,
                                context,
                                List.of("trace", "restart", StringArgumentType.getString(context, "session")))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> sessionCommand(
            ModTraceCoordinator coordinator, String name) {
        var session = ClientCommands.argument("session", StringArgumentType.word())
                .suggests(suggestSessions(coordinator))
                .executes(context -> run(
                        coordinator,
                        context,
                        List.of("trace", name, StringArgumentType.getString(context, "session"))));
        if ("export".equals(name)) {
            session.then(ClientCommands.argument("flags", StringArgumentType.greedyString())
                    .suggests(suggestExportFlags())
                    .executes(context -> run(
                            coordinator,
                            context,
                            ModClientTabCompleter.exportArgs(
                                    StringArgumentType.getString(context, "session"),
                                    StringArgumentType.getString(context, "flags")))));
        }
        return ClientCommands.literal(name)
                .executes(context -> run(coordinator, context, List.of("trace", name)))
                .then(session);
    }

    private static int run(
            ModTraceCoordinator coordinator,
            CommandContext<FabricClientCommandSource> context,
            List<String> args) {
        List<ModChatLine> lines = ModClientCommands.execute(coordinator, ownerName(), args);
        for (ModChatLine line : lines) {
            context.getSource().sendFeedback(ModChatRenderer.render(line));
        }
        FabricToasts.command(args, lines);
        return 1;
    }

    private static String ownerName() {
        var player = Minecraft.getInstance().player;
        return player == null ? "client" : player.getName().getString();
    }

    private static SuggestionProvider<FabricClientCommandSource> suggestEvents() {
        return (context, builder) -> {
            for (String name : ModClientTabCompleter.matchingEventNames(builder.getRemaining())) {
                builder.suggest(name);
            }
            return builder.buildFuture();
        };
    }

    private static SuggestionProvider<FabricClientCommandSource> suggestMods(ModTraceCoordinator coordinator) {
        return (context, builder) -> SharedSuggestionProvider.suggest(
                ModClientTabCompleter.complete(coordinator, List.of("mod"), builder.getRemaining()), builder);
    }

    private static SuggestionProvider<FabricClientCommandSource> suggestSessions(ModTraceCoordinator coordinator) {
        return (context, builder) -> SharedSuggestionProvider.suggest(
                ModClientTabCompleter.complete(coordinator, List.of("trace", "view"), builder.getRemaining()), builder);
    }

    private static SuggestionProvider<FabricClientCommandSource> suggestStartFlags(ModTraceCoordinator coordinator) {
        return (context, builder) -> {
            String event = StringArgumentType.getString(context, "event");
            for (String suggestion : ModClientTabCompleter.completeStartFlagSuggestions(event, builder.getRemaining())) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };
    }

    private static SuggestionProvider<FabricClientCommandSource> suggestExportFlags() {
        return (context, builder) -> {
            String session = StringArgumentType.getString(context, "session");
            for (String suggestion :
                    ModClientTabCompleter.completeExportFlagSuggestions(session, builder.getRemaining())) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };
    }
}
