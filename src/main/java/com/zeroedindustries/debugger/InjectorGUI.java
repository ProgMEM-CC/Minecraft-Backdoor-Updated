package com.zeroedindustries.debugger;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public class InjectorGUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    private JComboBox<String> lafSelector;

    // Map of LAF display names to fully qualified class names
    private final Map<String, String> lookAndFeels = new LinkedHashMap<>();

    // Wizard pages
    private File selectedJarFile;
    private String minecraftUUIDs;
    private boolean useUsernames;
    private String chatPrefix;
    private String discordWebhook;
    private boolean injectOther;
    private boolean warnings;

    // Current step index
    private int step = 0;

    // Constants for total steps (updated due to added offline mode page)
    private final int TOTAL_STEPS = 7;

    // Progress bar components
    private JPanel progressPanel;
    private JLabel[] stepCircles;
    private JLabel[] stepLabels;

    public InjectorGUI(Frame owner) {
        super(owner, "Zeroed Industries Injector Wizard", true);

        // Setup the configurable LAF map here:
        lookAndFeels.put("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        lookAndFeels.put("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
        lookAndFeels.put("System", UIManager.getSystemLookAndFeelClassName());

        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 450);
        setLocationRelativeTo(owner);
        updateButtons();
        updateProgressBar();
    }

    public static void displayError(String message){
        JOptionPane.showMessageDialog(null, message, "Zeroed Industries", JOptionPane.ERROR_MESSAGE);
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add wizard step panels
        cardPanel.add(createWelcomePanel(), "welcome");
        cardPanel.add(createFileChooserPanel(), "filechooser");
        cardPanel.add(createOfflineModePanel(), "offlinemode"); // New step for offline mode
        cardPanel.add(createUUIDsPanel(), "uuids");
        cardPanel.add(createChatPrefixPanel(), "prefix");
        cardPanel.add(createDiscordPanel(), "discord");
        cardPanel.add(createOptionsPanel(), "options");
        cardPanel.add(createSummaryPanel(), "summary");

        // Buttons with icons
        backButton = new JButton("Back", UIManager.getIcon("OptionPane.errorIcon"));
        nextButton = new JButton("Next", UIManager.getIcon("OptionPane.informationIcon"));
        cancelButton = new JButton("Cancel", UIManager.getIcon("OptionPane.warningIcon"));

        backButton.addActionListener(this::onBack);
        nextButton.addActionListener(this::onNext);
        cancelButton.addActionListener(e -> dispose());

        // Create LAF selector dropdown dynamically from map keys
        lafSelector = new JComboBox<>(lookAndFeels.keySet().toArray(new String[0]));
        lafSelector.setSelectedItem("Nimbus");  // Default
        lafSelector.addActionListener(e -> onChangeLookAndFeel());

        // Button panel with LAF selector on left, buttons on right
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel lafPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lafPanel.add(new JLabel("Look & Feel: "));
        lafPanel.add(lafSelector);

        JPanel navPanel = new JPanel();
        navPanel.add(backButton);
        navPanel.add(nextButton);
        navPanel.add(cancelButton);

        buttonPanel.add(lafPanel, BorderLayout.WEST);
        buttonPanel.add(navPanel, BorderLayout.EAST);

        // Progress bar panel at top
        progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        progressPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        progressPanel.setBackground(new Color(240, 240, 240));

        stepCircles = new JLabel[TOTAL_STEPS];
        stepLabels = new JLabel[TOTAL_STEPS];
        String[] stepNames = {
                "Welcome", "File", "Offline Mode", "UUIDs",
                "Prefix", "Discord", "Options", "Summary"
        };

        // Create circles with numbers and labels under each
        for (int i = 0; i < TOTAL_STEPS; i++) {
            JPanel stepPanel = new JPanel(new BorderLayout());
            stepPanel.setOpaque(false);

            JLabel circle = new JLabel(String.valueOf(i+1), SwingConstants.CENTER);
            circle.setPreferredSize(new Dimension(30, 30));
            circle.setOpaque(true);
            circle.setBackground(Color.LIGHT_GRAY);
            circle.setForeground(Color.DARK_GRAY);
            circle.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            circle.setFont(circle.getFont().deriveFont(Font.BOLD, 16f));

            JLabel label = new JLabel(stepNames[i], SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));

            stepPanel.add(circle, BorderLayout.CENTER);
            stepPanel.add(label, BorderLayout.SOUTH);

            progressPanel.add(stepPanel);

            stepCircles[i] = circle;
            stepLabels[i] = label;
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(progressPanel, BorderLayout.NORTH);
        getContentPane().add(cardPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Set default LAF
        setLookAndFeel("Nimbus");
    }

    // Wizard steps as JPanels

    private JPanel createWelcomePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><h2>Welcome to Zeroed Industries Injector</h2>" +
                "<p>This wizard will guide you through the injection process.</p></html>", SwingConstants.CENTER);
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFileChooserPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Select Spigot plugin .jar file to patch:");
        JTextField filePathField = new JTextField();
        filePathField.setEditable(false);
        JButton browseBtn = new JButton("Browse...", UIManager.getIcon("FileView.directoryIcon"));

        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase(Locale.ROOT).endsWith(".jar");
                }

                @Override
                public String getDescription() {
                    return "Spigot Plugin File (*.jar)";
                }
            });

            int result = fc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedJarFile = fc.getSelectedFile();
                filePathField.setText(selectedJarFile.getAbsolutePath());
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(label, BorderLayout.NORTH);
        topPanel.add(filePathField, BorderLayout.CENTER);
        topPanel.add(browseBtn, BorderLayout.EAST);

        p.add(topPanel, BorderLayout.NORTH);

        return p;
    }

    // New offline mode selection panel (radio buttons)
    private JPanel createOfflineModePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("<html><h3>Select Authentication Mode</h3>" +
                "<p>Please select whether to use offline mode (usernames) or online mode (UUIDs).</p></html>");
        p.add(label, BorderLayout.NORTH);

        JRadioButton onlineModeBtn = new JRadioButton("Use Online Mode (UUIDs)", !useUsernames);
        JRadioButton offlineModeBtn = new JRadioButton("Use Offline Mode (Usernames)", useUsernames);

        ButtonGroup group = new ButtonGroup();
        group.add(onlineModeBtn);
        group.add(offlineModeBtn);

        JPanel radioPanel = new JPanel(new GridLayout(2, 1));
        radioPanel.add(onlineModeBtn);
        radioPanel.add(offlineModeBtn);

        p.add(radioPanel, BorderLayout.CENTER);

        // Save selection on panel hide
        p.putClientProperty("onlineModeBtn", onlineModeBtn);
        p.putClientProperty("offlineModeBtn", offlineModeBtn);

        return p;
    }

    private JPanel createUUIDsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Minecraft UUIDs/Usernames (comma separated, leave blank to disable authorization):");
        JTextField uuidsField = new JTextField();

        p.add(label, BorderLayout.NORTH);
        p.add(uuidsField, BorderLayout.CENTER);

        p.putClientProperty("uuidTextField", uuidsField);

        return p;
    }

    private JPanel createChatPrefixPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Chat Command Prefix:");
        JTextField prefixField = new JTextField("#");

        p.add(label, BorderLayout.NORTH);
        p.add(prefixField, BorderLayout.CENTER);

        p.putClientProperty("prefixField", prefixField);

        return p;
    }

    private JPanel createDiscordPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Discord Webhook URL (leave blank to disable, recommended to disable):");
        JTextField discordField = new JTextField();

        p.add(label, BorderLayout.NORTH);
        p.add(discordField, BorderLayout.CENTER);

        p.putClientProperty("discordField", discordField);

        return p;
    }

    private JPanel createOptionsPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JCheckBox injectOtherCheck = new JCheckBox("Inject to other plugins? (Experimental, not working yet)");
        JCheckBox warningsCheck = new JCheckBox("Enable Debug Messages? (Use for GitHub issues)");

        p.add(injectOtherCheck);
        p.add(warningsCheck);

        p.putClientProperty("injectOtherCheck", injectOtherCheck);
        p.putClientProperty("warningsCheck", warningsCheck);

        return p;
    }

    private JPanel createSummaryPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        summaryArea.setBackground(new Color(245,245,245));
        summaryArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        p.add(new JLabel("Summary:"), BorderLayout.NORTH);
        p.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        p.putClientProperty("summaryArea", summaryArea);

        return p;
    }

    // Button listeners and navigation

    private void onBack(ActionEvent e) {
        if (step > 0) {
            step--;
            cardLayout.previous(cardPanel);
            updateButtons();
            updateProgressBar();
        }
    }

    private void onNext(ActionEvent e) {
        if (!validateStep()) {
            return;
        }
        if (step < TOTAL_STEPS - 1) {
            step++;
            cardLayout.next(cardPanel);
            updateButtons();
            updateProgressBar();

            if (step == TOTAL_STEPS - 1) {
                populateSummary();
            }
        } else {
            // Finish
            performInjection();
            dispose();
        }
    }

    private boolean validateStep() {
        switch (step) {
            case 1: // File chooser step
                if (selectedJarFile == null) {
                    displayError("Please select a .jar file to continue.");
                    return false;
                }
                break;
            case 2: { // Offline mode step
                JPanel p = (JPanel) cardPanel.getComponent(step);
                JRadioButton onlineBtn = (JRadioButton) p.getClientProperty("onlineModeBtn");
                JRadioButton offlineBtn = (JRadioButton) p.getClientProperty("offlineModeBtn");

                if (onlineBtn == null || offlineBtn == null) {
                    displayError("Internal error: Could not find offline mode buttons.");
                    return false;
                }
                useUsernames = offlineBtn.isSelected();
                break;
            }
            case 3: { // UUIDs/Usernames input
                JPanel p = (JPanel) cardPanel.getComponent(step);
                JTextField tf = (JTextField) p.getClientProperty("uuidTextField");
                if (tf != null) {
                    minecraftUUIDs = tf.getText().trim();
                }
                break;
            }
            case 4: { // Chat prefix
                JPanel p = (JPanel) cardPanel.getComponent(step);
                JTextField tf = (JTextField) p.getClientProperty("prefixField");
                if (tf != null) {
                    chatPrefix = tf.getText().trim();
                    if (chatPrefix.isEmpty()) {
                        displayError("Chat prefix cannot be empty.");
                        return false;
                    }
                }
                break;
            }
            case 5: { // Discord webhook
                JPanel p = (JPanel) cardPanel.getComponent(step);
                JTextField tf = (JTextField) p.getClientProperty("discordField");
                if (tf != null) {
                    discordWebhook = tf.getText().trim();
                }
                break;
            }
            case 6: { // Options checkboxes
                JPanel p = (JPanel) cardPanel.getComponent(step);
                JCheckBox injectCheck = (JCheckBox) p.getClientProperty("injectOtherCheck");
                JCheckBox warningsCheck = (JCheckBox) p.getClientProperty("warningsCheck");

                injectOther = injectCheck != null && injectCheck.isSelected();
                warnings = warningsCheck != null && warningsCheck.isSelected();
                break;
            }
        }
        return true;
    }

    private void updateButtons() {
        backButton.setEnabled(step > 0);
        nextButton.setText(step == TOTAL_STEPS - 1 ? "Finish" : "Next");
    }

    private void updateProgressBar() {
        for (int i = 0; i < TOTAL_STEPS; i++) {
            if (i == step) {
                stepCircles[i].setBackground(new Color(59, 89, 152));
                stepCircles[i].setForeground(Color.WHITE);
                stepCircles[i].setBorder(BorderFactory.createLineBorder(new Color(59, 89, 152), 2));
                stepLabels[i].setForeground(new Color(59, 89, 152));
            } else if (i < step) {
                stepCircles[i].setBackground(new Color(34, 139, 34)); // Dark green for completed steps
                stepCircles[i].setForeground(Color.WHITE);
                stepCircles[i].setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 2));
                stepLabels[i].setForeground(new Color(34, 139, 34));
            } else {
                stepCircles[i].setBackground(Color.LIGHT_GRAY);
                stepCircles[i].setForeground(Color.DARK_GRAY);
                stepCircles[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
                stepLabels[i].setForeground(Color.DARK_GRAY);
            }
        }
    }

    private void populateSummary() {
        JPanel p = (JPanel) cardPanel.getComponent(step);
        JTextArea summary = (JTextArea) p.getClientProperty("summaryArea");

        StringBuilder sb = new StringBuilder();
        sb.append("Selected File: ").append(selectedJarFile != null ? selectedJarFile.getAbsolutePath() : "None").append("\n");
        sb.append("Authentication Mode: ").append(useUsernames ? "Offline Mode (Usernames)" : "Online Mode (UUIDs)").append("\n");
        sb.append("UUIDs/Usernames: ").append(minecraftUUIDs == null || minecraftUUIDs.isEmpty() ? "None (No authorization)" : minecraftUUIDs).append("\n");
        sb.append("Chat Command Prefix: ").append(chatPrefix).append("\n");
        sb.append("Discord Webhook URL: ").append((discordWebhook == null || discordWebhook.isEmpty()) ? "Disabled" : discordWebhook).append("\n");
        sb.append("Inject Other Plugins: ").append(injectOther ? "Yes" : "No").append("\n");
        sb.append("Debug Messages: ").append(warnings ? "Enabled" : "Disabled").append("\n");

        summary.setText(sb.toString());
    }

    private void performInjection() {
        // TODO: Perform the actual injection logic here
        JOptionPane.showMessageDialog(this, "Injection process completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onChangeLookAndFeel() {
        String selected = (String) lafSelector.getSelectedItem();
        setLookAndFeel(selected);
    }

    private void setLookAndFeel(String lafName) {
        try {
            UIManager.setLookAndFeel(lookAndFeels.get(lafName));
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            displayError("Failed to set Look & Feel: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            InjectorGUI dlg = new InjectorGUI(null);
            dlg.setVisible(true);
        });
    }
}
