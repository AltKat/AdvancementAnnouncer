package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.PlayerData;
import io.github.altkat.advancementannouncer.guis.MainMenuGUI;
import org.bukkit.ChatColor;
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

        if (args.length > 0 && args[0].equalsIgnoreCase("edit")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("advancementannouncer.admin")) {
                    MainMenuGUI.open((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            }
            return true;
        }

        if (!sender.hasPermission("advancementannouncer.admin")) {
            if (args.length != 1 || !args[0].equalsIgnoreCase("toggle")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.wrong-usage")));
                return true;
            } else {
                if (!sender.hasPermission("advancementannouncer.toggle")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                handleToggle(sender);
            }
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("advancementannouncer.toggle")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            handleToggle(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            AutoAnnounce.stopAutoAnnounce();
            plugin.reloadConfig();
            AutoAnnounce.startAutoAnnounce();
            PlayerData.reloadPlayerData();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.config-reloaded")));
            return true;
        }

        if (args[0].equalsIgnoreCase("send")) {
            handleSendCommand(sender, args);
            return true;
        }

        sendHelpMessage(sender);
        return true;
    }

    private void handleToggle(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return;
        }
        UUID playerUUID = ((Player) sender).getUniqueId();
        PlayerData.setToggleData(playerUUID, !PlayerData.returnToggleData(playerUUID));
        if (PlayerData.returnToggleData(playerUUID)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-on")));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-off")));
        }
    }

    private void handleSendCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendHelpMessage(sender);
            return;
        }

        if (args[1].equalsIgnoreCase("preset")) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /aa send preset <presetName> <target>");
                return;
            }
            String presetName = args[2];
            String targetName = args[3];

            ConfigurationSection presetSection = plugin.getConfig().getConfigurationSection("presets." + presetName);
            if (presetSection == null) {
                sender.sendMessage(ChatColor.RED + "Preset not found: " + presetName);
                return;
            }

            String message;
            String styleStr;
            String iconStr;

            if (plugin.getConfig().isConfigurationSection("presets." + presetName)) {
                message = presetSection.getString("message");
                styleStr = presetSection.getString("style", "GOAL");
                iconStr = presetSection.getString("icon", "STONE");
            } else {
                message = plugin.getConfig().getString("presets." + presetName);
                styleStr = "GOAL";
                iconStr = "PAPER";
            }

            AdvancementHandler.Style style;
            try {
                style = AdvancementHandler.Style.valueOf(styleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                style = AdvancementHandler.Style.GOAL;
            }

            sendToTarget(sender, targetName, message, style, iconStr);
            return;
        }

        if (args.length >= 5) {
            final AdvancementHandler.Style style;
            try {
                style = AdvancementHandler.Style.valueOf(args[1].toUpperCase());
            } catch (final IllegalArgumentException t) {
                sender.sendMessage(ChatColor.RED + "Invalid style or option: " + args[1]);
                sender.sendMessage(ChatColor.GRAY + "Did you mean '/aa send preset'?");
                return;
            }

            final String materialName = args[2];
            try {
                Material.valueOf(materialName.toUpperCase());
            } catch (final IllegalArgumentException t) {
                sender.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
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
                sender.sendMessage(ChatColor.RED + "Could not find message for preset or custom input.");
                return;
            }

            sendToTarget(sender, targetName, message, style, materialName);
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage 1: /aa send preset <presetName> <target>");
        sender.sendMessage(ChatColor.YELLOW + "Usage 2: /aa send <style> <icon> <target> <message/presetName>");
    }

    private void sendToTarget(CommandSender sender, String targetName, String message, AdvancementHandler.Style style, String icon) {
        if (targetName.equalsIgnoreCase("all")) {
            if (sender.getServer().getOnlinePlayers().isEmpty()) {
                sender.sendMessage(ChatColor.RED + "There are no online players in the server!");
                return;
            }
            int sentCount = 0;
            for (Player player : sender.getServer().getOnlinePlayers()) {
                if (!PlayerData.returnToggleData(player.getUniqueId())) {
                    continue;
                }
                AdvancementHandler.displayTo(player, icon.toLowerCase(), message, style);
                sentCount++;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to " + sentCount + " player(s)"));
        } else {
            Player player = sender.getServer().getPlayer(targetName);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return;
            }
            AdvancementHandler.displayTo(player, icon.toLowerCase(), message, style);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to " + player.getName()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final List<String> tab = new ArrayList<>();
        if (!sender.hasPermission("advancementannouncer.admin")) {
            if (args.length == 1) {
                tab.add("toggle");
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
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aCommands: "));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa send preset <presetName> <player/all>"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa send <style> <material> <player/all> <message/presetName>"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa reload"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa toggle"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa edit"));
    }
}