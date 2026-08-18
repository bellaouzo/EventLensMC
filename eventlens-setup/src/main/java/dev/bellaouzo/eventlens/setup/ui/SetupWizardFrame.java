package dev.bellaouzo.eventlens.setup.ui;

import dev.bellaouzo.eventlens.setup.DestinationResolver;
import dev.bellaouzo.eventlens.setup.InstallRequest;
import dev.bellaouzo.eventlens.setup.InstallResult;
import dev.bellaouzo.eventlens.setup.SetupInstaller;
import dev.bellaouzo.eventlens.setup.SetupPaths;
import dev.bellaouzo.eventlens.setup.SetupTarget;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serial;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public final class SetupWizardFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String CARD_TARGET = "target";
    private static final String CARD_FOLDER = "folder";
    private static final String CARD_AGENT = "agent";
    private static final String CARD_DONE = "done";

    private final String version;
    private final transient SetupInstaller installer;
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final JLabel heading = new JLabel();
    private final JButton back = new JButton("Back");
    private final JButton next = new JButton("Next");
    private final JRadioButton paper = new JRadioButton("Paper server (plugins folder)", true);
    private final JRadioButton neoForge = new JRadioButton("NeoForge client (mods / Prism instance)");
    private final JRadioButton forge = new JRadioButton("Forge client (mods / Prism instance)");
    private final JRadioButton fabric = new JRadioButton("Fabric client (mods / Prism instance)");
    private final JTextField destinationField = new JTextField();
    private final JLabel destinationHint = new JLabel(" ");
    private final JCheckBox agentBox = new JCheckBox("Install the Java agent for precise timing (recommended)", true);
    private final JTextField agentField = new JTextField();
    private final SetupDonePage donePage = new SetupDonePage();
    private boolean doneNeedsManualJvm;
    private String card = CARD_TARGET;

    public SetupWizardFrame(String version, SetupInstaller installer) {
        super("EventLens setup " + version);
        this.version = version;
        this.installer = installer;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(720, 520));
        build();
        showCard(CARD_TARGET);
        pack();
        setLocationRelativeTo(null);
    }

    private void build() {
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        deck.add(targetPage(), CARD_TARGET);
        deck.add(folderPage(), CARD_FOLDER);
        deck.add(agentPage(), CARD_AGENT);
        deck.add(donePage, CARD_DONE);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        back.addActionListener(e -> goBack());
        next.addActionListener(e -> goNext());
        buttons.add(back);
        buttons.add(next);
        add(heading, BorderLayout.NORTH);
        add(deck, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private JPanel targetPage() {
        ButtonGroup group = new ButtonGroup();
        JPanel page = new JPanel();
        page.setLayout(new javax.swing.BoxLayout(page, javax.swing.BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        page.add(new JLabel("What are you installing?"));
        for (JRadioButton button : new JRadioButton[] {paper, neoForge, forge, fabric}) {
            group.add(button);
            page.add(button);
        }
        page.add(new JLabel(" "));
        page.add(new JLabel("This does not create a new Minecraft launcher profile."));
        page.add(new JLabel("Pick an existing server, mods folder, or Prism/MultiMC instance."));
        return page;
    }

    private JPanel folderPage() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        JButton browse = new JButton("Browse…");
        browse.addActionListener(e -> choose(destinationField));
        destinationField.getDocument().addDocumentListener(SimpleDocumentListener.on(this::refreshHint));
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(destinationField, BorderLayout.CENTER);
        row.add(browse, BorderLayout.EAST);
        page.add(row, BorderLayout.NORTH);
        page.add(destinationHint, BorderLayout.CENTER);
        return page;
    }

    private JPanel agentPage() {
        JPanel page = new JPanel();
        page.setLayout(new javax.swing.BoxLayout(page, javax.swing.BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        agentBox.addActionListener(e -> agentField.setEnabled(agentBox.isSelected()));
        page.add(agentBox);
        page.add(new JLabel("Agent folder (not plugins/ or mods/):"));
        JButton browse = new JButton("Browse…");
        browse.addActionListener(e -> choose(agentField));
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(agentField, BorderLayout.CENTER);
        row.add(browse, BorderLayout.EAST);
        page.add(row);
        page.add(new JLabel("Paper: writes user_jvm_args.txt and patches start.bat if present."));
        page.add(new JLabel("Prism/MultiMC: writes JvmArgs on instance.cfg."));
        page.add(new JLabel("CurseForge/Modrinth: copy the JVM line from the last page."));
        return page;
    }

    private void showCard(String id) {
        card = id;
        cards.show(deck, id);
        back.setEnabled(!CARD_TARGET.equals(id) && !CARD_DONE.equals(id));
        if (CARD_AGENT.equals(id)) {
            next.setText("Install");
        } else if (CARD_DONE.equals(id)) {
            next.setText("Close");
        } else {
            next.setText("Next");
        }
        heading.setHorizontalAlignment(SwingConstants.LEFT);
        heading.setText(
                switch (id) {
                    case CARD_TARGET -> "EventLens " + version;
                    case CARD_FOLDER ->
                        target().paper()
                                ? "Where is your Paper server or plugins folder?"
                                : "Where is your mods folder or Prism instance?";
                    case CARD_AGENT -> "Java agent";
                    default -> doneNeedsManualJvm ? "Action required" : "Installed";
                });
        if (CARD_FOLDER.equals(id)) {
            refreshHint();
        }
        if (CARD_AGENT.equals(id)
                && agentField.getText().isBlank()
                && !destinationField.getText().isBlank()) {
            agentField.setText(SetupPaths.defaultAgentDirectory(target(), Path.of(destinationField.getText()))
                    .toString());
        }
    }

    private void goBack() {
        if (CARD_FOLDER.equals(card)) {
            showCard(CARD_TARGET);
        } else if (CARD_AGENT.equals(card)) {
            showCard(CARD_FOLDER);
        }
    }

    private void goNext() {
        if (CARD_TARGET.equals(card)) {
            showCard(CARD_FOLDER);
        } else if (CARD_FOLDER.equals(card)) {
            if (destination().isEmpty()) {
                warn("Choose your server, plugins, mods, or Prism instance folder.");
                return;
            }
            showCard(CARD_AGENT);
        } else if (CARD_AGENT.equals(card)) {
            runInstall();
        } else {
            dispose();
        }
    }

    private void runInstall() {
        Path dest = destination().orElse(null);
        if (dest == null) {
            warn("Choose a destination folder.");
            return;
        }
        Path agentDir = agentBox.isSelected() ? parsePath(agentField.getText()) : null;
        if (agentBox.isSelected() && agentDir == null) {
            warn("Choose a folder for the agent jar.");
            return;
        }
        InstallResult result =
                installer.install(new InstallRequest(target(), dest, agentBox.isSelected(), agentDir, version));
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, result.lines().getFirst(), "Install failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        doneNeedsManualJvm = result.needsManualJvmPaste();
        donePage.display(result);
        showCard(CARD_DONE);
    }

    private SetupTarget target() {
        if (neoForge.isSelected()) {
            return SetupTarget.NEOFORGE;
        }
        if (forge.isSelected()) {
            return SetupTarget.FORGE;
        }
        if (fabric.isSelected()) {
            return SetupTarget.FABRIC;
        }
        return SetupTarget.PAPER;
    }

    private java.util.Optional<Path> destination() {
        Path path = parsePath(destinationField.getText());
        return path == null ? java.util.Optional.empty() : java.util.Optional.of(path);
    }

    private void refreshHint() {
        destination()
                .ifPresentOrElse(
                        path -> destinationHint.setText(DestinationResolver.describe(target(), path)),
                        () -> destinationHint.setText("Browse to the folder, then click Next."));
    }

    private void choose(JTextField field) {
        FolderPicker.choose(this, parsePath(field.getText()))
                .ifPresent(
                        path -> field.setText(path.toAbsolutePath().normalize().toString()));
    }

    private static Path parsePath(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return Path.of(text.trim());
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "EventLens setup", JOptionPane.WARNING_MESSAGE);
    }
}
