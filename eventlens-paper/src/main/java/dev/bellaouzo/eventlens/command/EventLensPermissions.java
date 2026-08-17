package dev.bellaouzo.eventlens.command;

import org.bukkit.command.CommandSender;

public final class EventLensPermissions {

    public static final String STATUS = "eventlens.command.status";
    public static final String LISTENERS = "eventlens.command.listeners";
    public static final String PLUGIN = "eventlens.command.plugin";
    public static final String INSTRUMENTATION = "eventlens.command.instrumentation";

    public static final String TRACE = "eventlens.command.trace";
    public static final String TRACE_START = "eventlens.command.trace.start";
    public static final String TRACE_STOP = "eventlens.command.trace.stop";
    public static final String TRACE_RESTART = "eventlens.command.trace.restart";
    public static final String TRACE_LIST = "eventlens.command.trace.list";
    public static final String TRACE_VIEW = "eventlens.command.trace.view";
    public static final String TRACE_EXPORT = "eventlens.command.trace.export";
    public static final String TRACE_EXPORT_FULL = "eventlens.command.trace.export.full";
    public static final String TRACE_HOT_EVENT = "eventlens.command.trace.hot-event";
    public static final String TRACE_HISTORY = "eventlens.command.trace.history";
    public static final String TRACE_FAVORITE = "eventlens.command.trace.favorite";
    public static final String TRACE_PRESETS = "eventlens.command.trace.presets";
    public static final String TRACE_LIVE = "eventlens.command.trace.live";

    private EventLensPermissions() {}

    public static boolean has(CommandSender sender, String node) {
        return sender.hasPermission(node);
    }

    public static boolean hasTrace(CommandSender sender, String action) {
        return has(sender, TRACE) || has(sender, TRACE + "." + action);
    }
}
