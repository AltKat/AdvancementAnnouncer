<div align="center">
  <h1><img src="https://altkat.github.io/AdvancementAnnouncer/new-logo-banner.png" alt="Title Image"></h1>
  <img src="https://altkat.github.io/AdvancementAnnouncer/greeting.png" alt="toast-example1"><br><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/cool.png" alt="toast-example2"><br><br>
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


**Advancement Announcer** gives you the ability to send toast advancement messages to desired players.

---

### Advancement Announcer Features
- **Full Color Support**: Supports all legacy colors (`&a`), gradient colors, and **Hex colors** (`&#RRGGBB`) in all messages and GUI elements.
- **Custom Sound Support**: Add custom sounds to any announcement. Supports both default Minecraft sounds (`ENTITY_PLAYER_LEVELUP`) and custom sounds from resource packs (`my.custom.sound`).
- **Custom Model Data (CMD) Support**: Display items with custom model data in your announcements. Fully supports integers (`12345`) and integrations with **ItemsAdder** and **Nexo** (`itemsadder:my_item` `nexo:my_item`).
- **Advanced Player Toggles**: Players can use `/aa toggle <announcements|sounds>` to independently mute announcements or just their sounds, allowing for silent notifications.
- **In-Game GUI Editor**: Manage everything from an easy-to-use in-game interface (`/aa edit`). No more YAML editing!
- **Join & First Join Messages**: Welcome players with unique, random-picker announcements when they first join or return.
- **Automated Messages**: Set up unlimited automated messages (presets or custom) to broadcast on a timer.
- **PlaceholderAPI (PAPI) Support**: Use any PAPI placeholders in your messages.
- **Update Checker**: Notifies admins when a new version is available.
- Tested on Minecraft versions **1.16.5 - 1.21.x**
- Feel free to contact me via <a href="https://discordapp.com/users/247441109888925697" target="_blank">Discord</a> for questions, issues, or suggestions.

---

### Commands
- **`/aa edit`**: Opens the main configuration GUI. From here, you can manage presets, auto-announcements, and join features.
- **`/aa send` Commands**:
    - **Send a Preset:** `/aa send preset <presetname> <target>`
        - Sends a pre-configured preset with its saved style, icon, sound, and CMD.
    - **Send Custom/Override:** `/aa send <style> <icon> <target> <message/presetname>`
        - Send a fully custom message OR override a preset's visual settings while keeping its message.
    - Possible options for **style**: `GOAL`, `TASK`, `CHALLENGE`.
    - Possible options for **icon** can be found <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html" target="_blank">here</a>.
    - Target a specific player's name or use `all`.
- **`/aa toggle <announcements|sounds>`**: Allows players to individually toggle whether they see announcements or hear their custom sounds.
- **`/aa reload`**: Reloads the configuration file.

### Permissions
- **`advancementannouncer.admin`**: Full access to all plugin features and commands.
- **`advancementannouncer.toggle`**: Parent permission to use the `/aa toggle` command.
    - **`advancementannouncer.toggle.announcements`**: Allows players to use `/aa toggle announcements`.
    - **`advancementannouncer.toggle.sounds`**: Allows players to use `/aa toggle sounds`.

---
### The In-Game GUI Editor
Manage everything with the new `/aa edit` command. Add, remove, or edit presets and auto-messages on the fly without ever needing to open the config file.

<div align="center">
  <img src="https://altkat.github.io/AdvancementAnnouncer/main_menu.gif" alt="main-menu-gif"><br>
</div>
<br>

### Images from Advancement Announcer

- Due to file limitations, GIFs are low quality
<div align="center">

#### Sending Presets

  <img src="https://altkat.github.io/AdvancementAnnouncer/new_preset_command.gif" alt="new-preset-command"><br>

#### Join Messages
  
<img src="https://altkat.github.io/AdvancementAnnouncer/join.gif" alt="join-feature-gif"><br>
  
#### Sending Custom Messages

<img src="https://altkat.github.io/AdvancementAnnouncer/new_send_command.gif" alt="new-send-command-gif"><br>
</div>
 
---

### Configuration (YAML)
While you can manage everything from the in-game GUI, you can still edit the `config.yml` manually.

<details>
  <summary>Click to see default config</summary>

```yaml
# ######################################################################################################
# ##                                                                                                  ##
# ##   AdvancementAnnouncer v${project.version} by Altkat(StreetMelodeez)                             ##
# ##   Discord: streetmelodeez                                                                        ##
# ##   Please use /aa reload to apply changes.                                                        ##
# ##   You can use /aa edit in game chat to edit this file.                                           ##
# ##                                                                                                  ##
# ######################################################################################################
#
# Custom Model Data Usage Examples:
#   custom-model-data: 123456 for direct integer.
#   custom-model-data: itemsadder:fire_sword for ItemsAdder.
#   custom-model-data: nexo:forest_sword for Nexo.
#
# ######################################################################################################
#
# Sound Usage:
#   You can add a sound to any announcement.
#   sound: "ENTITY_PLAYER_LEVELUP"
#   A list of sounds can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
#   Leave "" for no sound.
#
# ######################################################################################################
#
# Hex Color Usage:
#   This plugin supports Hex colors.
#   Use the format &#RRGGBB (e.g., &#FF0000) in any message.
#
# ######################################################################################################
#
# Auto-Announce & Message Options:
#   enabled: true/false - should the plugin announce messages automatically.
#   interval: in seconds - time between auto-announcements.
#   mode: "ORDERED" or "RANDOM" - the order of messages.
#   style: "GOAL", "TASK", or "CHALLENGE" - the frame style of the toast.
#   icon: Any valid Material name from Spigot.
#     (e.g., "DIAMOND", "GOLDEN_APPLE")
#     List: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
#
# ######################################################################################################
#
# Placeholder Usage:
#   You can use placeholders from PlaceholderAPI in the messages. like %player_name%
#
# ######################################################################################################
#
# How to print multiple lines:
#   Use | to print on a new line (If you print a one line message top line will be minecraft's default line (e.g, Goal Reached!)
#
# ######################################################################################################

presets:
  preset1:
    message: "&aThis is &6my cool| &amessage wow!"
    style: "GOAL"
    icon: "DIAMOND"
    custom-model-data: ""
    sound: "ENTITY_PLAYER_LEVELUP"
  store-preset:
    message: "&6You can get ranks|&6on our store &b/store"
    style: "TASK"
    icon: "EMERALD"
    custom-model-data: ""
    sound: ""
  discord-preset:
    message: "&9Join our discord server|&bdiscord.gg/yourdc"
    style: "CHALLENGE"
    icon: "BOOK"
    custom-model-data: ""
    sound: ""
  greeting:
    message: "&#54DAF4H&#54BBE5e&#549CD5l&#547DC6l&#545EB6o &e%player_name% &#54DAF4H&#54D2F0o&#54C9ECw
        &#54B9E3i&#54B1DFs &#54A0D7i&#5498D3t &#5487CBg&#547FC7o&#5477C2i&#546FBEn&#5466BAg&#545EB6?"
    style: GOAL
    icon: SUNFLOWER
    custom-model-data: ""
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"


auto-announce:
  enabled: false
  interval: 30
  mode: "ORDERED"
  messages:
    custommessage:
      message: "&9Join our discord server|&bdiscord.gg/yourdc"
      style: "TASK"
      icon: "DIAMOND"
      custom-model-data: ""
      sound: ""
    vipmessage:
      message: "&6You can get ranks|&6on our store &b/store"
      style: "CHALLENGE"
      icon: "GOLD_INGOT"
      custom-model-data: ""
      sound: ""


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
        custom-model-data: ""
        sound: ""
      welcome-back-2:
        message: '&7[&a+&7] &f%player_name%'
        style: TASK
        icon: TOTEM_OF_UNDYING
        custom-model-data: ""
        sound: ""

  first-join-messages:
    enabled: true
    messages:
      first-join-1:
        message: '&dWelcome to the server, &b%player_name%!'
        style: CHALLENGE
        icon: CAKE
        custom-model-data: ""
        sound: ""
      first-join-2:
        message: '&b%player_name% &ejoined for the first time!'
        style: GOAL
        icon: TOTEM_OF_UNDYING
        custom-model-data: ""
        sound: ""



#These are the messages shown to players
#You can edit these messages
lang-messages:
  plugin-prefix: "&#7688FF[Advancement Announcer] &r"
  config-reloaded: "&#76FF90Config reloaded!"
  wrong-usage: "&#F86B6BWrong usage! Please use &e/aa toggle <announcements|sounds>"
  announcements-toggled-on: "&#76FF90You now see the advancement announcements!"
  announcements-toggled-off: "&#F86B6BYou no longer see the advancement announcements!"
  sounds-toggled-on: "&#76FF90Announcement sounds are now enabled!"
  sounds-toggled-off: "&#F86B6BAnnouncement sounds are now disabled!"
  no-permission: "&#F86B6BYou don't have permission to use this command."

```

</details>

Need help? Feel free to contact me via <a href="https://discordapp.com/users/247441109888925697" target="_blank">Discord</a>.
