<div align="center">
  <h1><img src="https://altkat.github.io/AdvancementAnnouncer/new-logo-banner.png" alt="Title Image" width="600"></h1>

### 🎉 Custom Toast Advancement Messages for Your Server

<img src="https://altkat.github.io/AdvancementAnnouncer/greeting.png" alt="toast-example1" width="500"><br><br>
<img src="https://altkat.github.io/AdvancementAnnouncer/cool.png" alt="toast-example2" width="500"><br><br>
<img src="https://altkat.github.io/AdvancementAnnouncer/c.png" alt="toast-example3" width="500"><br><br>
</div>

---

<p align="center">
  <strong>Create stunning advancement announcements with full color support, custom sounds, and model data!</strong>
</p>

<p align="center">
  <br/>
  <img src="https://img.shields.io/badge/MC-1.16.5+-green?style=for-the-badge&logo=minecraft" alt="Minecraft 1.21+" />
  <img src="https://img.shields.io/badge/Java-17+-blueviolet?style=for-the-badge&logo=java" alt="Java 17+" />
  <a href="https://github.com/altkat/AdvancementAnnouncer/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge" alt="License: MIT" />
  </a>
  <a href="https://bstats.org/plugin/bukkit/AdvancementAnnouncer/24282">
    <img src="https://img.shields.io/bstats/servers/24282?label=bStats&style=for-the-badge" alt="bStats Servers" />
  </a>
  <br>
  <a href="https://discordapp.com/users/247441109888925697">
    <img src="https://img.shields.io/badge/Discord-💬%20Contact%20Me-5865F2?style=for-the-badge&logo=discord" alt="Discord Profile" />
  </a>
  <a href="https://github.com/altkat/AdvancementAnnouncer">
    <img src="https://img.shields.io/github/stars/altkat/AdvancementAnnouncer?style=for-the-badge&logo=github" alt="GitHub Stars" />
  </a>
</p>

---

## ✨ Features

<table>
  <tr>
    <td width="50%">
      <h3>🎨 Full Color Support</h3>
      <p>Legacy colors (<code>&a</code>), gradients, and <strong>Hex colors</strong> (<code>&#RRGGBB</code>) in all messages and GUI elements</p>
    </td>
    <td width="50%">
      <h3>🔊 Custom Sounds</h3>
      <p>Add custom sounds to announcements. Supports default Minecraft sounds and custom resource pack sounds</p>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>✏️ Custom Model Data (CMD)</h3>
      <p>Display items with custom model data. Supports integers, <strong>ItemsAdder</strong>, and <strong>Nexo</strong></p>
    </td>
    <td width="50%">
      <h3>🎮 Advanced Player Toggles</h3>
      <p>Players can independently toggle announcements or just sounds with <code>/aa toggle</code></p>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>⚙️ In-Game GUI Editor</h3>
      <p>Manage everything from an easy-to-use interface (<code>/aa edit</code>). No YAML editing needed!</p>
    </td>
    <td width="50%">
      <h3>👋 Join Messages</h3>
      <p>Welcome players with unique announcements on join or first join with random selection</p>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>⏰ Automated Messages</h3>
      <p>Set up unlimited auto-broadcast messages on a timer with ORDERED or RANDOM mode</p>
    </td>
    <td width="50%">
      <h3>🔌 PlaceholderAPI Support</h3>
      <p>Use any PAPI placeholders in your messages for dynamic content</p>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>📦 Multiple Presets</h3>
      <p>Create and manage unlimited message presets with different styles and settings</p>
    </td>
    <td width="50%">
      <h3>🔄 Update Checker</h3>
      <p>Automatic notifications for new versions available</p>
    </td>
  </tr>
</table>

---

## 📋 Commands

### 🎯 Main Commands

| Command | Usage | Permission | Description |
|---------|-------|-----------|-------------|
| `/aa edit` | Open GUI | `advancementannouncer.admin` | Opens the main configuration GUI |
| `/aa reload` | Reload config | `advancementannouncer.admin` | Reloads the configuration file |
| `/aa toggle` | Toggle settings | `advancementannouncer.toggle` | Toggle announcements or sounds |

### 📤 Send Commands

```
/aa send preset <presetName> <target>
```
Sends a pre-configured preset with saved style, icon, sound, and custom model data.

```
/aa send <style> <icon> <target> <message/presetName>
```
Send a fully custom message or override a preset's visual settings.

**Options:**
- **Style:** `GOAL`, `TASK`, `CHALLENGE`
- **Icon:** Any valid [Material name](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html)
- **Target:** Player name or `all`

### 🔀 Toggle Command

```
/aa toggle <announcements|sounds>
```
- `announcements` - Toggle visibility of announcements
- `sounds` - Toggle announcement sounds (silently notify)

---

## 🔐 Permissions

```yaml
advancementannouncer.admin
  ├─ Full access to all commands and GUI
  └─ Includes all sub-permissions

advancementannouncer.toggle
  ├─ advancementannouncer.toggle.announcements
  │  └─ Toggle announcements visibility
  └─ advancementannouncer.toggle.sounds
     └─ Toggle announcement sounds
```

---

## 🎮 GUI Editor

The in-game GUI makes configuration effortless! Access it with `/aa edit`

<div align="center">
  <img src="https://altkat.github.io/AdvancementAnnouncer/main_menu.gif" alt="main-menu-gif" width="600"><br>
</div>

### Features:
✅ Add, edit, and delete presets without YAML  
✅ Configure auto-announcements on the fly  
✅ Set up join/first-join messages  
✅ Real-time preview of messages  

---

## 📸 Demo Screenshots

<div align="center">

### Sending Presets
<img src="https://altkat.github.io/AdvancementAnnouncer/new_preset_command.gif" alt="new-preset-command" width="600"><br>

### Join Messages
<img src="https://altkat.github.io/AdvancementAnnouncer/join.gif" alt="join-feature-gif" width="600"><br>

### Sending Custom Messages
<img src="https://altkat.github.io/AdvancementAnnouncer/new_send_command.gif" alt="new-send-command-gif" width="600"><br>

</div>

---

## ⚙️ Configuration

All settings are editable through the in-game GUI or the `config.yml` file.

<details>
  <summary><strong>📝 Click to expand config.yml</strong></summary>

```yaml
# ######################################################################################################
# ##                                                                                                  ##
# ##   AdvancementAnnouncer v1.6.0 by Altkat(StreetMelodeez)                                          ##
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
#   Recommendation: Use sounds with only TASK and GOAL types since CHALLENGE type has its own sound effect.
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
    message: "&aThis is my &6cool|&amessage wow!"
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
      sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    custommessage2:
      message: "&eHello|&eHow is it going?"
      style: "GOAL"
      icon: "EMERALD"
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
        sound: "ENTITY_PLAYER_LEVELUP"
      welcome-back-2:
        message: '&7[&a+&7] &f%player_name%'
        style: TASK
        icon: TOTEM_OF_UNDYING
        custom-model-data: ""
        sound: "ENTITY_EXPERIENCE_ORB_PICKUP"

  first-join-messages:
    enabled: true
    messages:
      first-join-1:
        message: '&dWelcome to the server, &b%player_name%!'
        style: CHALLENGE
        icon: CAKE
        custom-model-data: ""
        sound: "ENTITY_PLAYER_LEVELUP"
      first-join-2:
        message: '&b%player_name% &ejoined for the first time!'
        style: GOAL
        icon: TOTEM_OF_UNDYING
        custom-model-data: ""
        sound: "ENTITY_EXPERIENCE_ORB_PICKUP"



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

---

## 🔧 Compatibility

| Server Type | Version | Status |
|------------|---------|--------|
| Paper | 1.16.5 - 1.21.x | ✅ Fully Supported |
| Spigot | 1.16.5 - 1.21.x | ✅ Fully Supported |

**Soft Dependencies:**
- PlaceholderAPI (Optional) - Enable placeholder support
- ItemsAdder (Optional) - Custom model data integration
- Nexo (Optional) - Custom model data integration

---

## 💬 Support

Have questions or found a bug? Reach out!

- **Discord:** [Message me](https://discordapp.com/users/247441109888925697)
- **Issues:** [GitHub Issues](https://github.com/altkat/AdvancementAnnouncer/issues)

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

**Third-party Libraries:**
- [UltimateAdvancementAPI](https://github.com/fren-gor/UltimateAdvancementAPI) - LGPL License
- [Adventure](https://github.com/KyoriPowered/adventure) - MIT License
- [bStats](https://bstats.org/) - MIT License


---

<div align="center">
  <p><strong>Made with ❤️ by AltKat (StreetMelodeez)</strong></p>
  <p><a href="https://discordapp.com/users/247441109888925697">💬 Contact</a> • <a href="https://github.com/altkat/AdvancementAnnouncer">⭐ Star</a> • <a href="https://github.com/altkat/AdvancementAnnouncer/issues">🐛 Report Bug</a></p>
</div>
