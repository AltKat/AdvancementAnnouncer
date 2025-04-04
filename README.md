<div align="center">
  <h1><img src="https://altkat.github.io/AdvancementAnnouncer/title.png" alt="Title Image"></h1>
  
</div>

<div align="center"><b>Important!</b></div><br>
<div align="center">This plugin uses Java 17, if you are using another java version you may need to add this line to your batch file:<br>
"<b>-DPaper.IgnoreJavaVersion=true ^</b>"​<br><br>
</div>

---

### Advancement Announcer Features
- **Advancement Announcer** gives you the ability to send toast advancement messages to desired players.
- You can set up **presets** to easily send them with a simple command.
- Set **automated messages** to send on a specified interval to your players.
- **AA** supports **PlaceholderAPI (PAPI)**, allowing you to use any placeholders supported by PAPI.
- Tested on Minecraft versions **1.16.5 - 1.21.4**.
- Feel free to contact me via <a href="https://discordapp.com/users/247441109888925697" target="_blank">Discord</a> for questions, issues, or suggestions.

---

### Commands
- **`/aa <style> <icon> <player's name/all> <preset/message>`**
  - Possible options for **style**: `GOAL`, `TASK`, `CHALLENGE`.
  - Possible options for **icon** can be found <a href="https://discordapp.com/users/247441109888925697" target="_blank">here</a>.
  - Target either a specific player's name or use `all`.
  - Write your own message in the command or use one of the presets defined in the config.
- **`/aa toggle`**: Players can set if they want to see announcement messages (Player-specific announcements are not affected by this command).
  - No permission required. 



### Permissions
- The only permission required: `advancementannouncer.admin`.

---
### Images from Advancement Announcer

<div align="center">
  <br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/a.png" alt="toast-example1"><br><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/b.png" alt="toast-example2"><br><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/c.png" alt="toast-example3"><br><br>
  <a>Due to file limits, GIFs are low quality and sped up 2x</a><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/autoannouncegif.gif" alt="command-gif0"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/fourthcommand.gif" alt="command-gif1"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/firstcommand.gif" alt="command-gif2"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/secondcommand.gif" alt="command-gif3"><br>
  <img src="https://altkat.github.io/AdvancementAnnouncer/thirdcommand.gif" alt="command-gif4"><br> 
</div>

 


---

### Configuration (YAML)

```yaml
###################################################################################################################################################
##                                           This plugin made by Altkat(StreetMelodeez) V1.1                                                     ##
##                                   Feel free to contact me for anything on discord: streetmelodeez                                             ##
##              Please use /aa reload to reload the plugin, or restart the server after you changed something in the config file.                ##
###################################################################################################################################################
bstats: true # Should the plugin send data to bStats?

# you can use placeholders from PlaceholderAPI in the messages. like %player_name%
# use | to print on a new line
presets: # you can add as many presets as you want
  preset1: "&aThis is &6my cool| &amessage wow!"
  store-preset: "&6You can get ranks|&6on our store &b/store"
  discord-preset: "&9Join our discord server|&bdiscord.gg/yourdc"
  greeting: "&eHello &a%player_name%|&eHow is it going?"


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




#These are the messages shown to players
#You can edit these messages
lang-messages:
  config-reloaded: "&3[AdvancementAnnouncer] &aConfig reloaded!"
  wrong-usage: "&cWrong usage! Please use /aa toggle"
  announcements-toggled-on: "&aYou now see the advancement announcements!"
  announcements-toggled-off: "&cYou no longer see the advancement announcements!"



```

Need help? Feel free to contact me via <a href="https://discordapp.com/users/247441109888925697" target="_blank">Discord</a>
