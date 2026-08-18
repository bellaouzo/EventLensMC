package dev.bellaouzo.eventlens.setup.ui;

import dev.bellaouzo.eventlens.setup.InstallResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

final class SetupDonePage extends JPanel {

    private static final Color REQUIRED_BORDER = new Color(180, 80, 0);
    private static final Color REQUIRED_FILL = new Color(255, 236, 179);
    private static final Color OPTIONAL_BORDER = new Color(70, 110, 160);
    private static final Color OPTIONAL_FILL = new Color(227, 236, 247);
    private static final Color SUMMARY_FILL = new Color(246, 248, 250);
    private static final Color SUMMARY_BORDER = new Color(200, 208, 216);

    private final JPanel jvmPanel = new JPanel();
    private final JLabel jvmTitle = new JLabel();
    private final JLabel jvmHint = new JLabel();
    private final JTextField jvmField = new JTextField();
    private final JButton copy = new JButton("Copy JVM argument");
    private final JButton readme = new JButton("Open README launcher steps");
    private final JEditorPane summary = new JEditorPane();

    SetupDonePage() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        buildJvmPanel();
        add(jvmPanel, BorderLayout.NORTH);
        add(summaryPanel(), BorderLayout.CENTER);
    }

    void display(InstallResult result) {
        summary.setText(SetupSummaryHtml.render(result.lines()));
        summary.setCaretPosition(0);
        if (!result.hasJvmArgument()) {
            jvmPanel.setVisible(false);
            return;
        }
        jvmField.setText(result.jvmArgument());
        jvmField.setCaretPosition(0);
        copy.setText("Copy JVM argument");
        if (result.needsManualJvmPaste()) {
            style(REQUIRED_FILL, REQUIRED_BORDER);
            jvmTitle.setText("Action required — paste this JVM argument");
            jvmHint.setText(
                    html(
                            """
                    The wizard could not edit your launcher automatically (common with CurseForge, \
                    Modrinth, or a plain .minecraft folder). Open your launcher → instance settings → \
                    <b>JVM arguments</b> (not game arguments, not mods/) and paste this one line.<br><br>\
                    If you have not done this before, the README has click-by-click steps for Prism, \
                    CurseForge, Modrinth, and start scripts."""));
        } else {
            style(OPTIONAL_FILL, OPTIONAL_BORDER);
            jvmTitle.setText("JVM argument (already written where we could)");
            jvmHint.setText(
                    html(
                            """
                    If /eventlens status still says dispatch-only, paste this same line into \
                    <b>JVM arguments</b> yourself. The README has per-launcher steps if you need them."""));
        }
        jvmPanel.setVisible(true);
    }

    private void buildJvmPanel() {
        jvmPanel.setLayout(new BoxLayout(jvmPanel, BoxLayout.Y_AXIS));
        jvmTitle.setFont(jvmTitle.getFont().deriveFont(Font.BOLD, 16f));
        jvmTitle.setAlignmentX(LEFT_ALIGNMENT);
        jvmHint.setAlignmentX(LEFT_ALIGNMENT);
        jvmField.setAlignmentX(LEFT_ALIGNMENT);
        jvmField.setEditable(false);
        jvmField.setColumns(48);
        jvmField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        jvmField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        copy.setAlignmentX(LEFT_ALIGNMENT);
        copy.addActionListener(e -> copyJvm());
        readme.setAlignmentX(LEFT_ALIGNMENT);
        readme.addActionListener(e -> SetupLinks.openReadmeAgents());
        jvmPanel.add(jvmTitle);
        jvmPanel.add(Box.createVerticalStrut(6));
        jvmPanel.add(jvmHint);
        jvmPanel.add(Box.createVerticalStrut(8));
        jvmPanel.add(jvmField);
        jvmPanel.add(Box.createVerticalStrut(8));
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(copy);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(readme);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        jvmPanel.add(actions);
        jvmPanel.setVisible(false);
    }

    private JPanel summaryPanel() {
        JLabel title = new JLabel("What we did");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        summary.setContentType("text/html");
        summary.setEditable(false);
        summary.setOpaque(true);
        summary.setBackground(SUMMARY_FILL);
        summary.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        JScrollPane scroll = new JScrollPane(summary);
        scroll.setBorder(BorderFactory.createLineBorder(SUMMARY_BORDER));
        scroll.getViewport().setBackground(SUMMARY_FILL);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void style(Color fill, Color border) {
        jvmPanel.setOpaque(true);
        jvmPanel.setBackground(fill);
        jvmPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 2), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        jvmTitle.setOpaque(false);
        jvmHint.setOpaque(false);
    }

    private void copyJvm() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(jvmField.getText()), null);
        copy.setText("Copied");
    }

    private static String html(String body) {
        return "<html><body style='width:520px'>" + body + "</body></html>";
    }
}
