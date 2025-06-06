package com.zeroedindustries.debugger;

import com.formdev.flatlaf.FlatIntelliJLaf;

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

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    private JComboBox<String> lafSelector;
    private JPanel progressBar;

    private final Map<String, String> lookAndFeels = new LinkedHashMap<>();

    private File selectedJarFile;
    private String minecraftUUIDs;
    private boolean useUsernames;
    private String chatPrefix;
    private String discordWebhook;
    private boolean injectOther;
    private boolean warnings;

    private int step = 0;
    private final int maxSteps = 7;

    public InjectorGUI(Frame owner) {
        super(owner, "Zeroed Industries Injector Wizard", true);

        lookAndFeels.put("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        lookAndFeels.put("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
        lookAndFeels.put("System", UIManager.getSystemLookAndFeelClassName());

        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(owner);
        updateButtons();
    }

    public static void displayError(String message){
        JOptionPane.showMessageDialog(null, message, "Zeroed Industries", JOptionPane.ERROR_MESSAGE);
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add panels
        cardPanel.add(createModeSelectionPanel(), "mode"); // New step 0
        cardPanel.add(createFileChooserPanel(), "filechooser");
        cardPanel.add(createUUIDsPanel(), "uuids");
        cardPanel.add(createChatPrefixPanel(), "prefix");
        cardPanel.add(createDiscordPanel(), "discord");
        cardPanel.add(createOptionsPanel(), "options");
        cardPanel.add(createSummaryPanel(), "summary");

        lafSelector = new JComboBox<>(lookAndFeels.keySet().toArray(new String[0]));
        lafSelector.setSelectedItem("Nimbus");
        lafSelector.addActionListener(e -> onChangeLookAndFeel());

        backButton = new JButton("Back");
        nextButton = new JButton("Next");
        cancelButton = new JButton("Cancel");

        backButton.addActionListener(this::onBack);
        nextButton.addActionListener(this::onNext);
        cancelButton.addActionListener(e -> dispose());

        progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = getWidth() - 40;
                int h = getHeight();
                int stepCount = maxSteps;
                int radius = 12;
                int spacing = w / (stepCount - 1);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 220, 220));
                g2.fillRoundRect(20, h / 2 - 4, w, 8, 8, 8);

                g2.setColor(new Color(66, 133, 244));
                int progressWidth = spacing * step;
                g2.fillRoundRect(20, h / 2 - 4, progressWidth, 8, 8, 8);

                for (int i = 0; i < stepCount; i++) {
                    int x = 20 + i * spacing;
                    g2.setColor(i <= step ? new Color(66, 133, 244) : new Color(180, 180, 180));
                    g2.fillOval(x - radius / 2, h / 2 - radius / 2, radius, radius);
                }
            }
        };
        progressBar.setPreferredSize(new Dimension(600, 30));

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
        getContentPane().add(progressBar, BorderLayout.NORTH);
        getContentPane().add(cardPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setLookAndFeel("Nimbus");
    }

    private JPanel createModeSelectionPanel() {
        JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Select Authentication Mode:");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JRadioButton uuidButton = new JRadioButton("Online mode (UUIDs)", true);
        JRadioButton nameButton = new JRadioButton("Offline mode (Usernames)");

        ButtonGroup group = new ButtonGroup();
        group.add(uuidButton);
        group.add(nameButton);

        p.add(title);
        p.add(uuidButton);
        p.add(nameButton);

        p.putClientProperty("uuidButton", uuidButton);
        p.putClientProperty("nameButton", nameButton);
        return p;
    }

    private JPanel createFileChooserPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Select Spigot plugin .jar file to patch:");
        JTextField filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setPreferredSize(new Dimension(400, 25));

        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase(Locale.ROOT).endsWith(".jar");
                }

                public String getDescription() {
                    return "Spigot Plugin File (*.jar)";
                }
            });

            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
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

        JLabel label = new JLabel("Enter UUIDs/Usernames (comma separated):");
        JTextField input = new JTextField();
        input.setPreferredSize(new Dimension(400, 25));

        p.add(label, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);

        p.putClientProperty("uuidTextField", input);
        return p;
    }

    private JPanel createChatPrefixPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Chat Command Prefix:");
        JTextField prefixField = new JTextField("#");
        prefixField.setPreferredSize(new Dimension(200, 25));

        p.add(label, BorderLayout.NORTH);
        p.add(prefixField, BorderLayout.CENTER);
        p.putClientProperty("prefixField", prefixField);
        return p;
    }

    private JPanel createDiscordPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Discord Webhook URL (leave blank to disable):");
        JTextField discordField = new JTextField();
        discordField.setPreferredSize(new Dimension(400, 25));

        p.add(label, BorderLayout.NORTH);
        p.add(discordField, BorderLayout.CENTER);
        p.putClientProperty("discordField", discordField);
        return p;
    }

    private JPanel createOptionsPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JCheckBox injectOtherCheck = new JCheckBox("Inject to other plugins? (Experimental)");
        JCheckBox warningsCheck = new JCheckBox("Enable Debug Messages?");

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

    private void onBack(ActionEvent e) {
        if (step > 0) {
            step--;
            cardLayout.previous(cardPanel);
            updateButtons();
            progressBar.repaint();
        }
    }

    private void onNext(ActionEvent e) {
        if (!validateCurrentStep()) return;

        updateStepData();
        if (step < maxSteps - 1) {
            step++;
            cardLayout.next(cardPanel);
            updateButtons();
            progressBar.repaint();
            if (step == maxSteps - 1) updateSummary();
        } else {
            performInjection();
        }
    }

    private void updateButtons() {
        backButton.setEnabled(step > 0);
        nextButton.setText(step == maxSteps - 1 ? "Finish" : "Next");
    }

    private void updateStepData() {
        switch (step) {
            case 0: 
                JPanel p = (JPanel) cardPanel.getComponent(0);
                useUsernames = ((JRadioButton) p.getClientProperty("nameButton")).isSelected();
                break;
            case 2:
                JPanel p = (JPanel) cardPanel.getComponent(2);
                JTextField t = (JTextField) p.getClientProperty("uuidTextField");
                minecraftUUIDs = t.getText().trim();
                break;
            case 3:
                JPanel p = (JPanel) cardPanel.getComponent(3);
                JTextField t = (JTextField) p.getClientProperty("prefixField");
                chatPrefix = t.getText().trim();
                break;
            case 4:
                JPanel p = (JPanel) cardPanel.getComponent(4);
                JTextField t = (JTextField) p.getClientProperty("discordField");
                discordWebhook = t.getText().trim();
                break;
            case 5:
                JPanel p = (JPanel) cardPanel.getComponent(5);
                injectOther = ((JCheckBox) p.getClientProperty("injectOtherCheck")).isSelected();
                warnings = ((JCheckBox) p.getClientProperty("warningsCheck")).isSelected();
                break;
        }
    }

    private boolean validateCurrentStep() {
        if (step == 1 && (selectedJarFile == null || !selectedJarFile.exists())) {
            JOptionPane.showMessageDialog(this, "Please select a valid .jar file to patch.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (step == 3) {
            JPanel p = (JPanel) cardPanel.getComponent(3);
            JTextField prefixField = (JTextField) p.getClientProperty("prefixField");
            if (prefixField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chat Command Prefix cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void updateSummary() {
        JPanel p = (JPanel) cardPanel.getComponent(6);
        JTextArea area = (JTextArea) p.getClientProperty("summaryArea");
        area.setText(
                "File to patch: " + (selectedJarFile == null ? "None" : selectedJarFile.getAbsolutePath()) + "\n" +
                        "Offline mode: " + useUsernames + "\n" +
                        "UUIDs/Usernames: " + (minecraftUUIDs == null || minecraftUUIDs.isEmpty() ? "(none)" : minecraftUUIDs) + "\n" +
                        "Chat Prefix: " + chatPrefix + "\n" +
                        "Discord Webhook: " + (discordWebhook.isEmpty() ? "(none)" : discordWebhook) + "\n" +
                        "Inject to others: " + injectOther + "\n" +
                        "Debug messages: " + warnings + "\n"
        );
    }

    private void performInjection() {
        JOptionPane.showMessageDialog(this, "Injection process would start now.\n(Not implemented yet)", "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void onChangeLookAndFeel() {
        String selected = (String) lafSelector.getSelectedItem();
        if (selected != null) setLookAndFeel(selected);
    }

    private void setLookAndFeel(String name) {
        String lafClass = lookAndFeels.get(name);
        if (lafClass == null) return;

        try {
            if ("System".equals(name)) {
                lafClass = UIManager.getSystemLookAndFeelClassName();
            }
            UIManager.setLookAndFeel(lafClass);
            SwingUtilities.updateComponentTreeUI(this);
            pack();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to set Look & Feel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new InjectorGUI(null).setVisible(true));
    }
}
