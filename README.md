<div align="center">
  <h1><img src="https://altkat.github.io/AdvancementAnnouncer/title.png" alt="Title Image"></h1>
  <img src="https://altkat.github.io/AdvancementAnnouncer/a.png" alt="toast-example1"><br><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/b.png" alt="toast-example2"><br><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/c.png" alt="toast-example3"><br><br>

</div>

<p align="center">
  <br/>
    <img src="https://img.shields.io/badge/MC-1.16.5+-green?style=for-the-badge" alt="Minecraft 1.21+" />
  <img src="https://img.shields.io/badge/Java-17+-blueviolet?style=for-the-badge" alt="Java 17+" />
  <a href="https://github.com/altkat/AdvancementAnnouncer/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge" alt="License: MIT" />
  </a>
  <a href="https://bstats.org/plugin/bukkit/AdvancementAnnouncer/24282">
    <img src="https://img.shields.io/bstats/servers/24282?label=bStats&style=for-the-badge" alt="bStats Servers" />
  </a>
  <br>
  <a href="https://discordapp.com/users/247441109888925697">
    <img src="https://img.shields.io/badge/Discord-Profile-5865F2?style=for-the-badge&logo=discord" alt="Discord Profile" />
  </a>
</p>

<div align="center"><b>Important!</b></div><br>
<div align="center">This plugin requires Java 17 or newer.​<br>
<br><br>
</div>

---

### Advancement Announcer Features
- **Advancement Announcer** gives you the ability to send toast advancement messages to desired players.
- **Join & First Join Messages**: Welcome your players with stylish custom announcements when they join the server.
- **In-Game GUI Editor**: Manage everything from an easy-to-use in-game interface. No more YAML editing!
- You can set up **presets** to easily send them with a simple command or the GUI.
- Set **automated messages** to send on a specified interval to your players.
- **AA** supports **PlaceholderAPI (PAPI)**, allowing you to use any placeholders supported by PAPI.
- **Update Checker**: Notifies admins in-game when a new version of the plugin is available.
- Tested on Minecraft versions **1.16.5 - 1.21.8**.
- Feel free to contact me via <a href="https://discordapp.com/users/247441109888925697" target="_blank">Discord</a> for questions, issues, or suggestions.

---

### Commands
- **`/aa edit`**: Opens the main configuration GUI. From here, you can manage presets and auto-announcements.
- **`/aa send` Commands**:
    - **Send a Preset:** `/aa send preset <presetname> <target>`
      - Sends a pre-configured preset with its saved style and icon.
    - **Send Custom/Override:** `/aa send <style> <icon> <target> <message/presetname>`
      - Send a fully custom message OR override a preset's visual settings while keeping its message.
    - Possible options for **style**: `GOAL`, `TASK`, `CHALLENGE`.
    - Possible options for **icon** can be found <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html" target="_blank">here</a>.
    - Target a specific player's name or use `all`.
- **`/aa toggle`**: Players can set if they want to see announcement messages (Player-specific announcements are not affected by this command).
- **`/aa reload`**: Reloads the configuration file.

### Permissions
- **`advancementannouncer.admin`**: Full access to all plugin features and commands.
- **`advancementannouncer.toggle`**: Allows players to use the `/aa toggle` command (Default: true).

---
### The In-Game GUI Editor
Manage everything with the new `/aa edit` command. Add, remove, or edit presets and auto-messages on the fly without ever needing to open the config file.

<div align="center">
  <img src="https://altkat.github.io/AdvancementAnnouncer/aaeditmenu.gif" alt="edit-gui-gif"><br>
</div>
<br>

### Images from Advancement Announcer

<div align="center">
  <a>Due to file limits, GIFs are low quality and sped up 2x</a><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/autoannouncegif.gif" alt="command-gif0"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/fourthcommand.gif" alt="command-gif1"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/firstcommand.gif" alt="command-gif2"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/secondcommand.gif" alt="command-gif3"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/thirdcommand.gif" alt="command-gif4"><br>
</div>
 
---

### Configuration (YAML)
While you can manage everything from the in-game GUI, you can still edit the `config.yml` manually.

<details>
  <summary>Click to see default config</summary>

```yaml
# ######################################################################################################
# ##                                                                                                  ##
# ##   AdvancementAnnouncer v1.5.0 by Altkat(StreetMelodeez)                                            ##
# ##   Discord: streetmelodeez                                                                        ##
# ##   Please use /aa reload to apply changes.                                                        ##
# ##   You can use /aa edit in game chat to edit this file.                                           ##
# ##                                                                                                  ##
# ######################################################################################################

# you can use placeholders from PlaceholderAPI in the messages. like %player_name%
# use | to print on a new line
presets: # you can add as many presets as you want
  preset1:
    message: "&aThis is &6my cool| &amessage wow!"
    style: "GOAL"
    icon: "DIAMOND"
  store-preset:
    message: "&6You can get ranks|&6on our store &b/store"
    style: "TASK"
    icon: "EMERALD"
  discord-preset:
    message: "&9Join our discord server|&bdiscord.gg/yourdc"
    style: "CHALLENGE"
    icon: "BOOK"
  greeting:
    message: "&eHello &a%player_name%|&eHow is it going?"
    style: "GOAL"
    icon: "APPLE"


auto-announce:
  enabled: false    # should the plugin announce messages automatically
  interval: 30      # in seconds
  mode: "ORDERED"   # ORDERED, RANDOM
  messages:         # you can add as many messages as you want
    custommessage1:
      message: "&eHello &a%player_name%|&eHow is it going?"
      style: "GOAL"   # GOAL, TASK, CHALLENGE
      icon: "EMERALD" # https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
    custommessage2:
      message: "&9Join our discord server|&bdiscord.gg/yourdc"
      style: "TASK"
      icon: "DIAMOND"
    vipmessage:
      message: "&6You can get ranks|&6on our store &b/store"
      style: "CHALLENGE"
      icon: "GOLD_INGOT"


# Messages to be displayed when players join the server.
# If you add more than one message, a random one will be selected and displayed.
join-features:
  join-messages:
    enabled: true
    messages:
      welcome-back-1:
        message: '&eWelcome back, &a%player_name%!'
        style: GOAL
        icon: GOLDEN_APPLE
      welcome-back-2:
        message: '&7[&a+&7] &f%player_name%'
        style: TASK
        icon: OAK_DOOR

  first-join-messages:
    enabled: true
    messages:
      first-join-1:
        message: '&dWelcome to the server, &b%player_name%!'
        style: CHALLENGE
        icon: CAKE
      first-join-2:
        message: '&b%player_name% &ejoined for the first time!'
        style: GOAL
        icon: TOTEM_OF_UNDYING



#These are the messages shown to players
#You can edit these messages
lang-messages:
  config-reloaded: "&3[AdvancementAnnouncer] &aConfig reloaded!"
  wrong-usage: "&cWrong usage! Please use /aa toggle"
  announcements-toggled-on: "&aYou now see the advancement announcements!"
  announcements-toggled-off: "&cYou no longer see the advancement announcements!"
  edit-gui-title: "&3Advancement Announcer Edit"
  presets-gui-title: "&3Advancement Announcer Presets"
  auto-announce-gui-title: "&3Auto Announce Config"
  input-cancelled: "&cInput process cancelled."

```

</details>

Need help? Feel free to contact me via <a href="https://discordapp.com/users/247441109888925697" target="_blank">Discord</a>.
