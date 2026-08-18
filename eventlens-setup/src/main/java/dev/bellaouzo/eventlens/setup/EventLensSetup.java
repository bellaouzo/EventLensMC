package dev.bellaouzo.eventlens.setup;

import dev.bellaouzo.eventlens.setup.ui.SetupWizardFrame;
import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class EventLensSetup {

    private static final Logger LOG = Logger.getLogger(EventLensSetup.class.getName());

    private EventLensSetup() {}

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            LOG.severe("EventLens setup needs a desktop window. Use a machine with a display.");
            System.exit(1);
            return;
        }
        SwingUtilities.invokeLater(EventLensSetup::open);
    }

    private static void open() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            LOG.log(Level.WARNING, "Using default look and feel", ex);
        }
        String version = SetupPaths.productVersion();
        SetupInstaller installer = new SetupInstaller(ArtifactLocator.create(version));
        SetupWizardFrame frame = new SetupWizardFrame(version, installer);
        frame.setVisible(true);
    }
}
