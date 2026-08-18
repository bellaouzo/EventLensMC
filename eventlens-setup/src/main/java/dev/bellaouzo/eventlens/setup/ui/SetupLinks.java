package dev.bellaouzo.eventlens.setup.ui;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

final class SetupLinks {

    /** Must stay aligned with {@code AgentInstallHints.README_URL} and the README heading. */
    static final String README_AGENTS = "https://github.com/bellaouzo/EventLensMC#java-agents-optional";

    private SetupLinks() {}

    static void openReadmeAgents() {
        try {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI.create(README_AGENTS));
                return;
            }
        } catch (java.io.IOException | RuntimeException ex) {
            java.util.logging.Logger.getLogger(SetupLinks.class.getName())
                    .log(java.util.logging.Level.FINE, "Could not open README in a browser", ex);
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(README_AGENTS), null);
    }
}
