package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.PlayerPreferencesService;
import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.application.TracePresetMerger;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.observability.SamplingPolicy;
import dev.bellaouzo.eventlens.domain.trace.TraceStartResult;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

final class TraceStartCommandHandler {

    private final TraceCommandService traceCommandService;
    private final EventLensCommandConfig commandConfig;
    private final PlayerPreferencesService playerPreferencesService;

    TraceStartCommandHandler(
            TraceCommandService traceCommandService,
            EventLensCommandConfig commandConfig,
            PlayerPreferencesService playerPreferencesService) {
        this.traceCommandService = traceCommandService;
        this.commandConfig = commandConfig;
        this.playerPreferencesService = playerPreferencesService;
    }

    void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, "start")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace start <event[,event]|--preset name> [--plugin name] [--player name] "
                            + "[--world name] [--region x1,z1,x2,z2] [--cancelled any|yes|no] [--max-events n] "
                            + "[--max-duration ns|nm] [--slow-threshold 1ms] [--capture-stacks] [--confirm-hot] "
                            + "[--generic] [--detail brief|normal|verbose]",
                    NamedTextColor.YELLOW));
            return;
        }

        try {
            int optionStart = optionStartIndex(args);
            String eventQuery = String.join(",", Arrays.copyOfRange(args, 2, optionStart));
            List<String> optionTokens =
                    new java.util.ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, optionStart, args.length)));
            TracePresetMerger.MergeResult mergeResult = TracePresetMerger.merge(commandConfig, optionTokens);
            if (mergeResult.hasError()) {
                sender.sendMessage(
                        Component.text(mergeResult.presetNotFoundError().orElseThrow(), NamedTextColor.RED));
                return;
            }
            if (eventQuery.isBlank() && !mergeResult.eventSimpleNames().isEmpty()) {
                eventQuery = String.join(",", mergeResult.eventSimpleNames());
            }
            if (eventQuery.isBlank()) {
                sender.sendMessage(
                        Component.text("Specify event names or a preset that includes events.", NamedTextColor.RED));
                return;
            }
            if (missingPluginWatchFilter(mergeResult)) {
                sender.sendMessage(Component.text(
                        "plugin-watch requires --plugin <name> or a preset plugin: value.", NamedTextColor.RED));
                return;
            }

            TraceCommandService.TraceStartOptions options =
                    TraceCommandService.TraceStartOptions.parse(mergeResult.tokens(), commandConfig);
            TraceStartResult result = traceCommandService.startTrace(eventQuery, sender.getName(), options);
            switch (result) {
                case TraceStartResult.Success(
                        String sessionId,
                        String eventClassName,
                        List<String> eventClassNames) -> {
                    String eventLabel = eventClassNames.stream()
                            .map(CommandText::simpleName)
                            .reduce((left, right) -> left + ", " + right)
                            .orElse(CommandText.simpleName(eventClassName));
                    sender.sendMessage(Component.text("Trace session started: " + sessionId, NamedTextColor.GREEN));
                    sender.sendMessage(CommandUi.labeledLine(
                            "Event",
                            CommandUi.runCommand(
                                    eventLabel, "/eventlens trace view " + sessionId, "View this trace session")));
                    if (commandConfig.showPerformanceWarnings()) {
                        renderPerformanceWarning(sender, eventClassName);
                    }
                    if (sender instanceof Player player) {
                        playerPreferencesService.recordTraceStart(player.getUniqueId(), sessionId, eventClassName);
                    }
                }
                case TraceStartResult.Failure failure -> renderFailure(sender, failure);
            }
        } catch (IllegalArgumentException ex) {
            String rawMessage = ex.getMessage();
            @NonNull String errorMessage = rawMessage == null ? "Invalid trace start arguments." : rawMessage;
            sender.sendMessage(Component.text(errorMessage, NamedTextColor.RED));
        }
    }

    private static boolean missingPluginWatchFilter(TracePresetMerger.MergeResult mergeResult) {
        return mergeResult.presetName().filter("plugin-watch"::equalsIgnoreCase).isPresent()
                && mergeResult.tokens().stream().noneMatch(token -> token.equalsIgnoreCase("--plugin"));
    }

    private static int optionStartIndex(String[] args) {
        if (args.length >= 3 && args[2].startsWith("--")) {
            return 2;
        }
        int index = 3;
        while (index < args.length && !args[index].startsWith("--")) {
            index++;
        }
        return index;
    }

    private void renderFailure(CommandSender sender, TraceStartResult.Failure failure) {
        if (failure.reason() == TraceStartResult.Failure.Reason.HOT_EVENT_CONFIRMATION) {
            if (!EventLensPermissions.hasTrace(sender, "hot-event")) {
                sender.sendMessage(Component.text(
                        "Hot-event tracing requires permission " + EventLensPermissions.TRACE_HOT_EVENT + ".",
                        NamedTextColor.RED));
                sender.sendMessage(Component.text(failure.message(), NamedTextColor.YELLOW));
                return;
            }
            HotEventPromptFormatter.render(sender, failure);
            return;
        }
        sender.sendMessage(Component.text(failure.message(), NamedTextColor.RED));
    }

    private void renderPerformanceWarning(CommandSender sender, String eventClassName) {
        if (SamplingPolicy.requiresNarrowingFilter(eventClassName)) {
            sender.sendMessage(Component.text(
                    "Hot-event trace active: expect sampling and measurable overhead on matching dispatches.",
                    NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text(
                "Tracing adds per-dispatch overhead. Stop the session when you are done investigating.",
                NamedTextColor.GRAY));
    }
}
