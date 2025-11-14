package io.github.altkat.advancementannouncer.feature;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.core.PlayerData;
import io.github.altkat.advancementannouncer.core.AdvancementHandler;
import io.github.altkat.advancementannouncer.editor.menu.MainMenuGUI;
import io.github.altkat.advancementannouncer.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {
    AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        final String prefix = plugin.getPrefix();

        if (args.length > 0 && args[0].equalsIgnoreCase("edit")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("advancementannouncer.admin")) {
                    MainMenuGUI.open((Player) sender);
                } else {
                    sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.no-permission", "&#F86B6BYou don't have permission to use this command.")));
                }
            } else {
                sender.sendMessage(prefix + TextUtil.color("&#F86B6BYou must be a player to use this command!"));
            }
            return true;
        }

        if (!sender.hasPermission("advancementannouncer.admin")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
                if (!sender.hasPermission("advancementannouncer.toggle")) {
                    sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.no-permission", "&#F86B6BYou don't have permission to use this command.")));
                    return true;
                }
                handleNewToggle(sender, args);
            } else {
                sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.wrong-usage", "&#F86B6BWrong usage! Please use /aa toggle")));
            }
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("advancementannouncer.toggle")) {
                sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.no-permission", "&#F86B6BYou don't have permission to use this command.")));
                return true;
            }
            handleNewToggle(sender, args);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            AutoAnnounce.stopAutoAnnounce();
            plugin.reloadConfig();
            plugin.clearPrefixCache();
            AutoAnnounce.startAutoAnnounce();
            PlayerData.reloadPlayerData();
            sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.config-reloaded")));
            return true;
        }

        if (args[0].equalsIgnoreCase("send")) {
            handleSendCommand(sender, args);
            return true;
        }

        sendHelpMessage(sender);
        return true;
    }

    private void handleNewToggle(CommandSender sender, String[] args) {
        final String prefix = plugin.getPrefix();
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + TextUtil.color("&cYou must be a player to use this command!"));
            return;
        }

        UUID playerUUID = ((Player) sender).getUniqueId();

        final String wrongUsageMsg = TextUtil.color(
                plugin.getConfig().getString("lang-messages.wrong-usage", "&cWrong usage! Please use /aa toggle <announcements|sounds>"));

        final String noPermMsg = prefix + TextUtil.color(
                plugin.getConfig().getString("lang-messages.no-permission", "&cYou don't have permission to use this command."));

        if (args.length == 1) {
            sender.sendMessage(prefix + wrongUsageMsg);
            return;
        }

        if (args[1].equalsIgnoreCase("announcements")) {

            if (!sender.hasPermission("advancementannouncer.toggle.announcements")) {
                sender.sendMessage(noPermMsg);
                return;
            }

            boolean newStatus = !PlayerData.returnToggleData(playerUUID);
            PlayerData.setToggleData(playerUUID, newStatus);

            if (newStatus) {
                sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.announcements-toggled-on", "&aAnnouncements are now enabled!")));
            } else {
                sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.announcements-toggled-off", "&cAnnouncements are now disabled!")));
            }

        } else if (args[1].equalsIgnoreCase("sounds")) {

            if (!sender.hasPermission("advancementannouncer.toggle.sounds")) {
                sender.sendMessage(noPermMsg);
                return;
            }

            boolean newStatus = !PlayerData.areSoundsEnabled(playerUUID);
            PlayerData.setSoundsEnabled(playerUUID, newStatus);

            if (newStatus) {
                sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.sounds-toggled-on", "&aAnnouncement sounds are now enabled!")));
            } else {
                sender.sendMessage(prefix + TextUtil.color(plugin.getConfig().getString("lang-messages.sounds-toggled-off", "&cAnnouncement sounds are now disabled! (You will still see announcements)")));
            }

        } else {
            sender.sendMessage(prefix + wrongUsageMsg);
        }
    }

    private void handleSendCommand(CommandSender sender, String[] args) {
        final String prefix = plugin.getPrefix();

        if (args.length < 2) {
            sendHelpMessage(sender);
            return;
        }

        if (args[1].equalsIgnoreCase("preset")) {
            if (args.length < 4) {
                sender.sendMessage(prefix + TextUtil.color("&#FCD05CPreset Usage: \n&7- &#76FF90/aa send preset <presetName> <target>"));
                return;
            }
            String presetName = args[2];
            String targetName = args[3];

            ConfigurationSection presetSection = plugin.getConfig().getConfigurationSection("presets." + presetName);
            if (presetSection == null) {
                sender.sendMessage(prefix + TextUtil.color("&#F86B6BPreset not found: " + presetName));
                return;
            }

            String message;
            String styleStr;
            String iconStr;
            String customModelDataStr;
            String soundStr;

            if (plugin.getConfig().isConfigurationSection("presets." + presetName)) {
                message = presetSection.getString("message");
                styleStr = presetSection.getString("style", "GOAL");
                iconStr = presetSection.getString("icon", "STONE");
                customModelDataStr = presetSection.getString("custom-model-data", null);
                soundStr = presetSection.getString("sound", "");
            } else {
                message = plugin.getConfig().getString("presets." + presetName);
                styleStr = "GOAL";
                iconStr = "PAPER";
                customModelDataStr = null;
                soundStr = "";
            }

            AdvancementHandler.Style style;
            try {
                style = AdvancementHandler.Style.valueOf(styleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                style = AdvancementHandler.Style.GOAL;
            }

            sendToTarget(sender, targetName, message, style, iconStr, customModelDataStr, soundStr);
            return;
        }

        if (args.length >= 5) {
            final AdvancementHandler.Style style;
            try {
                style = AdvancementHandler.Style.valueOf(args[1].toUpperCase());
            } catch (final IllegalArgumentException t) {
                sender.sendMessage(prefix + TextUtil.color("&#F86B6BInvalid style or option: " + args[1]));
                sender.sendMessage(prefix + TextUtil.color("&#FCD05CDid you mean '/aa send preset'?"));
                return;
            }

            final String materialName = args[2];
            try {
                Material.valueOf(materialName.toUpperCase());
            } catch (final IllegalArgumentException t) {
                sender.sendMessage(prefix + TextUtil.color("&#F86B6BInvalid material: " + materialName));
                return;
            }

            String targetName = args[3];
            String message = "";

            String potentialPresetName = args[4];
            if (plugin.getConfig().isConfigurationSection("presets." + potentialPresetName)) {
                message = plugin.getConfig().getString("presets." + potentialPresetName + ".message");
            } else if (plugin.getConfig().isString("presets." + potentialPresetName)) {
                message = plugin.getConfig().getString("presets." + potentialPresetName);
            } else {
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 4; i < args.length; i++) {
                    messageBuilder.append(args[i]).append(" ");
                }
                message = messageBuilder.toString().trim();
            }

            if (message == null || message.isEmpty()) {
                sender.sendMessage(prefix + TextUtil.color("&#F86B6BCould not find message for preset or custom input."));
                return;
            }

            sendToTarget(sender, targetName, message, style, materialName, null, null);
            return;
        }

        sender.sendMessage(prefix + TextUtil.color("&#FCD05CSend Usages: \n&#FCD05CUsage 1: \n&7- &#76FF90/aa send preset <presetName> <target>"));
        sender.sendMessage(TextUtil.color("&#FCD05CUsage 2: \n&7- &#76FF90/aa send <style> <icon> <target> <message/presetName>"));
    }

    private void sendToTarget(CommandSender sender, String targetName, String message, AdvancementHandler.Style style, String icon, String customModelData, String sound) {
        final String prefix = plugin.getPrefix();
        if (targetName.equalsIgnoreCase("all")) {
            if (sender.getServer().getOnlinePlayers().isEmpty()) {
                sender.sendMessage(prefix + TextUtil.color("&#F86B6BThere are no online players in the server!"));
                return;
            }
            int sentCount = 0;
            for (Player player : sender.getServer().getOnlinePlayers()) {
                if (!PlayerData.returnToggleData(player.getUniqueId())) {
                    continue;
                }
                AdvancementHandler.displayTo(player, icon.toLowerCase(), customModelData, message, style, sound);
                sentCount++;
            }
            sender.sendMessage(prefix + TextUtil.color("&#76FF90Advancement message sent to &#FCD05C" + sentCount + " &#76FF90player(s)"));
        } else {
            Player player = sender.getServer().getPlayer(targetName);
            if (player == null) {
                sender.sendMessage(prefix + TextUtil.color("&#F86B6BPlayer not found: " + targetName));
                return;
            }
            AdvancementHandler.displayTo(player, icon.toLowerCase(), customModelData, message, style, sound);
            sender.sendMessage(prefix + TextUtil.color("&#76FF90Advancement message sent to &#FCD05C" + player.getName()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final List<String> tab = new ArrayList<>();
        if (!sender.hasPermission("advancementannouncer.admin")) {
            if (args.length == 1) {
                if ("toggle".startsWith(args[0].toLowerCase())) {
                    tab.add("toggle");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
                if ("announcements".startsWith(args[1].toLowerCase())) {
                    tab.add("announcements");
                }
                if ("sounds".startsWith(args[1].toLowerCase())) {
                    tab.add("sounds");
                }
            }
            return tab;
        }

        if (args.length == 1) {
            tab.add("reload");
            tab.add("toggle");
            tab.add("edit");
            tab.add("send");
            return filter(tab, args);
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length == 2) {
                tab.add("announcements");
                tab.add("sounds");
            }
        }

        if (args[0].equalsIgnoreCase("send")) {
            switch (args.length) {
                case 2:
                    tab.add("preset");
                    for (final AdvancementHandler.Style style : AdvancementHandler.Style.values()) {
                        tab.add(style.toString().toLowerCase());
                    }
                    break;
                case 3:
                    if (args[1].equalsIgnoreCase("preset")) {
                        if (plugin.getConfig().getConfigurationSection("presets") != null) {
                            tab.addAll(plugin.getConfig().getConfigurationSection("presets").getKeys(false));
                        }
                    } else if (isStyle(args[1])) {
                        for (final Material material : Material.values()) {
                            if (material.isItem() && material != Material.AIR) {
                                tab.add(material.toString().toLowerCase());
                            }
                        }
                    }
                    break;
                case 4:
                    tab.add("all");
                    for (final Player player : sender.getServer().getOnlinePlayers()) {
                        tab.add(player.getName());
                    }
                    break;
                case 5:
                    if (isStyle(args[1])) {
                        if (plugin.getConfig().getConfigurationSection("presets") != null) {
                            tab.addAll(plugin.getConfig().getConfigurationSection("presets").getKeys(false));
                        }
                        tab.add("<message>");
                    }
                    break;
            }
        }

        return filter(tab, args);
    }

    private List<String> filter(List<String> list, String[] args) {
        return list.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private boolean isStyle(String arg) {
        try {
            AdvancementHandler.Style.valueOf(arg.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        final String prefix = plugin.getPrefix();
        sender.sendMessage(prefix + TextUtil.color("&#FCD05CCommands: "));
        sender.sendMessage(TextUtil.color("&#FCD05C-----------------------------------------------------"));
        sender.sendMessage(TextUtil.color("&#FCD05C Sends a Configured Preset \n &7- &#76FF90/aa send preset <presetName> <player/all>\n "));
        sender.sendMessage(TextUtil.color("&#FCD05C Sends a Custom Message / Overrides a Configured Preset \n &7- &#76FF90/aa send <style> <material> <player/all> <message/presetName>\n "));
        sender.sendMessage(TextUtil.color("&#FCD05C Reloads AdvancementAnnouncer Plugin \n &7- &#76FF90/aa reload\n "));
        sender.sendMessage(TextUtil.color("&#FCD05C Toggles Announcement Display / Announcement Sounds \n &7- &#76FF90/aa toggle <announcements|sounds>\n "));
        sender.sendMessage(TextUtil.color("&#FCD05C Opens AdvancementAnnouncer GUI \n &7- &#76FF90/aa edit"));
        sender.sendMessage(TextUtil.color("&#FCD05C-----------------------------------------------------"));
    }
}