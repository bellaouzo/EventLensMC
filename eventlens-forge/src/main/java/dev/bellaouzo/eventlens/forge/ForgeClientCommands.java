package dev.bellaouzo.eventlens.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.command.ModClientCommands;
import dev.bellaouzo.eventlens.modcommon.command.ModClientTabCompleter;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensNotices;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;

public final class ForgeClientCommands {

    private final ModTraceCoordinator coordinator;

    public ForgeClientCommands(ModTraceCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(root("eventlens"));
        dispatcher.register(root("el"));
    }

    private LiteralArgumentBuilder<CommandSourceStack> root(String name) {
        return Commands.literal(name)
                .executes(context -> run(context, List.of("status")))
                .then(Commands.literal("status").executes(context -> run(context, List.of("status"))))
                .then(Commands.literal("ui").executes(context -> {
                    EventLensScreen.open();
                    return 1;
                }))
                .then(listeners())
                .then(trace());
    }

    private LiteralArgumentBuilder<CommandSourceStack> listeners() {
        return Commands.literal("listeners")
                .executes(context -> run(context, List.of("listeners")))
                .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(suggestEvents())
                        .executes(context -> run(
                                context, List.of("listeners", StringArgumentType.getString(context, "event")))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> trace() {
        return Commands.literal("trace")
                .executes(context -> run(context, List.of("trace")))
                .then(start())
                .then(sessionCommand("stop"))
                .then(sessionCommand("pause"))
                .then(sessionCommand("resume"))
                .then(restart())
                .then(Commands.literal("list").executes(context -> run(context, List.of("trace", "list"))))
                .then(view())
                .then(sessionCommand("export"));
    }

    private LiteralArgumentBuilder<CommandSourceStack> start() {
        return Commands.literal("start")
                .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(suggestEvents())
                        .executes(context ->
                                run(context, List.of("trace", "start", StringArgumentType.getString(context, "event"))))
                        .then(Commands.argument("flags", StringArgumentType.greedyString())
                                .suggests(suggestStartFlags())
                                .executes(context -> {
                                    List<String> args = new ArrayList<>();
                                    args.add("trace");
                                    args.add("start");
                                    args.add(StringArgumentType.getString(context, "event"));
                                    args.addAll(List.of(StringArgumentType.getString(context, "flags").split("\\s+")));
                                    return run(context, args);
                                })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> view() {
        return Commands.literal("view")
                .then(Commands.argument("session", StringArgumentType.word())
                        .suggests(suggestSessions())
                        .executes(context ->
                                run(context, List.of("trace", "view", StringArgumentType.getString(context, "session"))))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> run(
                                        context,
                                        List.of(
                                                "trace",
                                                "view",
                                                StringArgumentType.getString(context, "session"),
                                                Integer.toString(IntegerArgumentType.getInteger(context, "page")))))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> restart() {
        return Commands.literal("restart")
                .then(Commands.argument("session", StringArgumentType.word())
                        .suggests(suggestSessions())
                        .executes(context -> run(
                                context,
                                List.of("trace", "restart", StringArgumentType.getString(context, "session")))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> sessionCommand(String name) {
        return Commands.literal(name)
                .executes(context -> run(context, List.of("trace", name)))
                .then(Commands.argument("session", StringArgumentType.word())
                        .suggests(suggestSessions())
                        .executes(context -> run(
                                context, List.of("trace", name, StringArgumentType.getString(context, "session")))));
    }

    private int run(CommandContext<CommandSourceStack> context, List<String> args) {
        String owner = context.getSource().getTextName();
        List<ModChatLine> lines = ModClientCommands.execute(coordinator, owner, args);
        for (ModChatLine line : lines) {
            context.getSource().sendSuccess(() -> ForgeChatRenderer.render(line), false);
        }
        EventLensNotices.command(args, lines);
        return 1;
    }

    private SuggestionProvider<CommandSourceStack> suggestEvents() {
        return (context, builder) -> {
            for (String name : ModClientTabCompleter.matchingEventNames(builder.getRemaining())) {
                builder.suggest(name);
            }
            return builder.buildFuture();
        };
    }

    private SuggestionProvider<CommandSourceStack> suggestSessions() {
        return (context, builder) -> SharedSuggestionProvider.suggest(
                ModClientTabCompleter.complete(coordinator, List.of("trace", "view"), builder.getRemaining()), builder);
    }

    private SuggestionProvider<CommandSourceStack> suggestStartFlags() {
        return (context, builder) -> SharedSuggestionProvider.suggest(
                ModClientTabCompleter.complete(
                        coordinator,
                        List.of("trace", "start", StringArgumentType.getString(context, "event")),
                        builder.getRemaining()),
                builder);
    }
}
