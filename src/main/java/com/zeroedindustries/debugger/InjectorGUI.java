package com.zeroedindustries.debugger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class InjectorGUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ProgressPanel progressPanel;

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    private final Map<String, String> lookAndFeels = new LinkedHashMap<>();

    private File selectedJarFile;
    private String minecraftUUIDs;
    private boolean useUsernames;
    private String chatPrefix;
    private String discordWebhook;
    private boolean injectOther;
    private boolean warnings;

    private int step = 0;
    private final String[] steps = {
            "Welcome", "Jar", "Mode", "UUIDs", "Prefix", "Discord", "Options", "Summary"
    };

    public InjectorGUI(Frame owner) {
        super(owner, "Zeroed Industries Injector Wizard", true);

        lookAndFeels.put("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");

        initComponents();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(owner);
        updateButtons();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add wizard step panels
        cardPanel.add(createWelcomePanel(), "welcome");
        cardPanel.add(createFileChooserPanel(), "jar");
        cardPanel.add(createModePanel(), "mode");
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

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel navPanel = new JPanel();
        navPanel.add(backButton);
        navPanel.add(nextButton);
        navPanel.add(cancelButton);

        buttonPanel.add(navPanel, BorderLayout.EAST);

        progressPanel = new ProgressPanel(steps, step);
        progressPanel.setPreferredSize(new Dimension(700, 40));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(progressPanel, BorderLayout.NORTH);
        getContentPane().add(cardPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setLookAndFeel("Nimbus");
    }

    // Custom panel for progress bar with rounded edges and circles
    private static class ProgressPanel extends JPanel {
        private final String[] steps;
        private int currentStep;

        public ProgressPanel(String[] steps, int currentStep) {
            this.steps = steps;
            this.currentStep = currentStep;
            setOpaque(false);
        }

        public void setCurrentStep(int step) {
            this.currentStep = step;
            repaint();
        }
        public void displayError(String message) {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();
            int stepsCount = steps.length;
            int segment = width / stepsCount;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int barHeight = 12;
            int circleDiameter = 16;
            int circleRadius = circleDiameter / 2;

            // Draw the rounded progress bar background line
            int lineY = height / 2 - barHeight / 2;

            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRoundRect(segment / 2, lineY, segment * (stepsCount - 1), barHeight, barHeight, barHeight);

            // Draw filled portion up to current step
            g2.setColor(new Color(100, 180, 255));
            int filledWidth = stepToX(currentStep, segment);
            g2.fillRoundRect(segment / 2, lineY, filledWidth - segment / 2, barHeight, barHeight, barHeight);

            // Draw step circles
            for (int i = 0; i < stepsCount; i++) {
                int centerX = i * segment + segment / 2;
                int centerY = height / 2;

                if (i <= currentStep) {
                    g2.setColor(new Color(30, 110, 220));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillOval(centerX - circleRadius, centerY - circleRadius, circleDiameter, circleDiameter);

                g2.setColor(new Color(100, 180, 255));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(centerX - circleRadius, centerY - circleRadius, circleDiameter, circleDiameter);
            }
        }

        private int stepToX(int step, int segment) {
            return step * segment + segment / 2;
        }
    }

    private JPanel createWelcomePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><h2>Welcome</h2><p>This wizard will guide you through injection setup.</p></html>", SwingConstants.CENTER);
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFileChooserPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Select plugin .jar file:");
        JTextField pathField = new JTextField(30);
        pathField.setEditable(false);
        pathField.setPreferredSize(new Dimension(250, 24)); // smaller input height

        JButton browse = new JButton("Browse...");
        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase(Locale.ROOT).endsWith(".jar");
                }

                public String getDescription() {
                    return "JAR Files (*.jar)";
                }
            });

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedJarFile = chooser.getSelectedFile();
                pathField.setText(selectedJarFile.getAbsolutePath());
            }
        });

        JPanel inner = new JPanel(new BorderLayout(5, 5));
        inner.add(label, BorderLayout.NORTH);
        inner.add(pathField, BorderLayout.CENTER);
        inner.add(browse, BorderLayout.EAST);

        p.add(inner, BorderLayout.NORTH);
        return p;
    }

    private JPanel createModePanel() {
        JPanel p = new JPanel();
        p.setBorder(new EmptyBorder(30, 20, 20, 20));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Select mode of authorization:");
        JRadioButton uuidButton = new JRadioButton("Use UUIDs (online mode)");
        JRadioButton usernameButton = new JRadioButton("Use Usernames (offline mode)");

        ButtonGroup group = new ButtonGroup();
        group.add(uuidButton);
        group.add(usernameButton);
        uuidButton.setSelected(true);

        p.add(label);
        p.add(uuidButton);
        p.add(usernameButton);

        p.putClientProperty("uuidRadio", uuidButton);
        return p;
    }

    private JPanel createUUIDsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("UUIDs/Usernames (comma-separated):");
        JTextField input = new JTextField();
        input.setPreferredSize(new Dimension(300, 24)); // smaller height

        p.add(label, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);

        p.putClientProperty("uuidField", input);
        return p;
    }

    private JPanel createChatPrefixPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Chat Command Prefix:");
        JTextField prefixField = new JTextField("#");
        prefixField.setPreferredSize(new Dimension(100, 24)); // small input box

        p.add(label, BorderLayout.NORTH);
        p.add(prefixField, BorderLayout.CENTER);

        p.putClientProperty("prefixField", prefixField);
        return p;
    }

    private JPanel createDiscordPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Discord Webhook URL (optional):");
        JTextField discordField = new JTextField();
        discordField.setPreferredSize(new Dimension(400, 24)); // small input box

        p.add(label, BorderLayout.NORTH);
        p.add(discordField, BorderLayout.CENTER);

        p.putClientProperty("discordField", discordField);
        return p;
    }

    private JPanel createOptionsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JCheckBox injectCheck = new JCheckBox("Inject to other plugins (experimental)");
        JCheckBox debugCheck = new JCheckBox("Enable debug messages");

        p.add(injectCheck);
        p.add(debugCheck);

        p.putClientProperty("injectCheck", injectCheck);
        p.putClientProperty("debugCheck", debugCheck);
        return p;
    }

    private JPanel createSummaryPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextArea summary = new JTextArea();
        summary.setEditable(false);
        summary.setFont(new Font("Monospaced", Font.PLAIN, 12));

        p.add(new JLabel("Summary:"), BorderLayout.NORTH);
        p.add(new JScrollPane(summary), BorderLayout.CENTER);

        p.putClientProperty("summaryArea", summary);
        return p;
    }

    private void updateStepData() {
        switch (step) {
            case 2:
                JPanel modePanel = (JPanel) cardPanel.getComponent(2);
                JRadioButton uuidRadio = (JRadioButton) modePanel.getClientProperty("uuidRadio");
                useUsernames = !uuidRadio.isSelected();
                break;
            case 3:
                JPanel uuidPanel = (JPanel) cardPanel.getComponent(3);
                JTextField uuidField = (JTextField) uuidPanel.getClientProperty("uuidField");
                minecraftUUIDs = uuidField.getText().trim();
                break;
            case 4:
                JPanel prefixPanel = (JPanel) cardPanel.getComponent(4);
                JTextField prefix = (JTextField) prefixPanel.getClientProperty("prefixField");
                chatPrefix = prefix.getText().trim();
                break;
            case 5:
                JPanel discordPanel = (JPanel) cardPanel.getComponent(5);
                JTextField discord = (JTextField) discordPanel.getClientProperty("discordField");
                discordWebhook = discord.getText().trim();
                break;
            case 6:
                JPanel optionPanel = (JPanel) cardPanel.getComponent(6);
                JCheckBox injectCheck = (JCheckBox) optionPanel.getClientProperty("injectCheck");
                JCheckBox debugCheck = (JCheckBox) optionPanel.getClientProperty("debugCheck");
                injectOther = injectCheck.isSelected();
                warnings = debugCheck.isSelected();
                break;
        }
    }

    private void updateSummary() {
        JPanel summaryPanel = (JPanel) cardPanel.getComponent(7);
        JTextArea area = (JTextArea) summaryPanel.getClientProperty("summaryArea");

        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(selectedJarFile != null ? selectedJarFile.getAbsolutePath() : "None").append("\n");
        sb.append("Use Usernames: ").append(useUsernames).append("\n");
        sb.append("UUIDs/Usernames: ").append(minecraftUUIDs).append("\n");
        sb.append("Prefix: ").append(chatPrefix).append("\n");
        sb.append("Discord Webhook: ").append(discordWebhook.isEmpty() ? "(none)" : discordWebhook).append("\n");
        sb.append("Inject to others: ").append(injectOther).append("\n");
        sb.append("Debug: ").append(warnings);

        area.setText(sb.toString());
    }

    private boolean validateCurrentStep() {
        if (step == 1 && (selectedJarFile == null || !selectedJarFile.exists())) {
            JOptionPane.showMessageDialog(this, "Please select a valid .jar file.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (step == 4) {
            JPanel prefixPanel = (JPanel) cardPanel.getComponent(4);
            JTextField prefix = (JTextField) prefixPanel.getClientProperty("prefixField");
            if (prefix.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Prefix is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void onBack(ActionEvent e) {
        if (step > 0) {
            step--;
            cardLayout.previous(cardPanel);
            updateButtons();
            progressPanel.setCurrentStep(step);
        }
    }

    private void onNext(ActionEvent e) {
        if (!validateCurrentStep()) return;
        updateStepData();

        if (step < steps.length - 1) {
            step++;
            cardLayout.next(cardPanel);
            if (step == steps.length - 1) updateSummary();
        } else {
            JOptionPane.showMessageDialog(this, "Injection would begin now.", "Info", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }

        updateButtons();
        progressPanel.setCurrentStep(step);
    }

    private void updateButtons() {
        backButton.setEnabled(step > 0);
        nextButton.setText(step == steps.length - 1 ? "Finish" : "Next");
    }

    private void setLookAndFeel(String name) {
        String laf = lookAndFeels.get(name);
        try {
            UIManager.setLookAndFeel(laf);
            SwingUtilities.updateComponentTreeUI(this);
            pack();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to set LAF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            InjectorGUI gui = new InjectorGUI(null);
            gui.setVisible(true);
        });
    }
}
