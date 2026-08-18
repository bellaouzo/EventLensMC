package dev.bellaouzo.eventlens.setup;

import java.util.List;

public record InstallResult(boolean success, List<String> lines, String jvmArgument, boolean jvmPatched) {

    public InstallResult {
        lines = List.copyOf(lines);
        if (jvmArgument != null && jvmArgument.isBlank()) {
            jvmArgument = null;
        }
    }

    static InstallResult ok(List<String> lines) {
        return new InstallResult(true, lines, null, false);
    }

    static InstallResult ok(List<String> lines, String jvmArgument, boolean jvmPatched) {
        return new InstallResult(true, lines, jvmArgument, jvmPatched);
    }

    static InstallResult fail(String message) {
        return new InstallResult(false, List.of(message), null, false);
    }

    public boolean hasJvmArgument() {
        return jvmArgument != null;
    }

    public boolean needsManualJvmPaste() {
        return hasJvmArgument() && !jvmPatched;
    }
}
