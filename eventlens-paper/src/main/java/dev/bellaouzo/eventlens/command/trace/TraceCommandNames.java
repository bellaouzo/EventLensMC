package dev.bellaouzo.eventlens.command.trace;

import java.util.List;

final class TraceCommandNames {

    static final String CORRELATE = "correlate";
    static final String COMPARE_BASELINE = "--compare-baseline";
    static final List<String> TRACE_SUBCOMMANDS = List.of(
            TraceCommandTabCompleter.SUBCOMMAND_START,
            TraceCommandTabCompleter.SUBCOMMAND_STOP,
            TraceCommandTabCompleter.SUBCOMMAND_RESTART,
            "list",
            "view",
            "export",
            "copy",
            TraceCommandTabCompleter.SUBCOMMAND_COMPARE,
            CORRELATE,
            "baseline",
            "history",
            "favorite",
            "presets",
            TraceCommandTabCompleter.SUBCOMMAND_LIVE);

    private TraceCommandNames() {}
}
