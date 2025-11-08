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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter, Listener {
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
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /aa send <style> <icon> <player/all> <message/preset>");
            return;
        }

        final AdvancementHandler.Style style;
        try {
            style = AdvancementHandler.Style.valueOf(args[1].toUpperCase());
        } catch (final Throwable t) {
            sender.sendMessage(ChatColor.RED + "Invalid style: " + args[1]);
            return;
        }

        final String materialName = args[2];
        try {
            Material.valueOf(materialName.toUpperCase());
        } catch (final Throwable t) {
            sender.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
            return;
        }

        String audience = args[3];
        String message = "";

        if (plugin.getConfig().getConfigurationSection("presets").getKeys(false).contains(args[4])) {
            message = plugin.getConfig().getString("presets." + args[4]);
        } else {
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 4; i < args.length; i++) {
                messageBuilder.append(args[i]).append(" ");
            }
            message = messageBuilder.toString();
        }
        message = ChatColor.translateAlternateColorCodes('&', message.trim());

        if (audience.equalsIgnoreCase("all")) {
            if (sender.getServer().getOnlinePlayers().isEmpty()) {
                sender.sendMessage(ChatColor.RED + "There are no online players in the server!");
                return;
            }
            int sentCount = 0;
            for (Player player : sender.getServer().getOnlinePlayers()) {
                if (!PlayerData.returnToggleData(player.getUniqueId())) {
                    continue;
                }
                AdvancementHandler.displayTo(player, materialName.toLowerCase(), message, style);
                sentCount++;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to " + sentCount + " player(s)"));
        } else {
            Player player = sender.getServer().getPlayer(audience);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + audience);
                return;
            }
            AdvancementHandler.displayTo(player, materialName.toLowerCase(), message, style);
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

        switch (args.length) {
            case 1:
                tab.add("reload");
                tab.add("toggle");
                tab.add("edit");
                tab.add("send");
                break;
            case 2:
                if (args[0].equalsIgnoreCase("send")) {
                    for (final AdvancementHandler.Style style : AdvancementHandler.Style.values())
                        tab.add(style.toString().toLowerCase());
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("send")) {
                    for (final Material material : Material.values())
                        if (material.isItem() && material != Material.AIR) {
                            tab.add(material.toString().toLowerCase());
                        }
                }
                break;
            case 4:
                if (args[0].equalsIgnoreCase("send")) {
                    tab.add("all");
                    for (final Player player : sender.getServer().getOnlinePlayers()) {
                        tab.add(player.getName());
                    }
                }
                break;
            case 5:
                if (args[0].equalsIgnoreCase("send")) {
                    tab.addAll(plugin.getConfig().getConfigurationSection("presets").getKeys(false));
                    tab.add("your message");
                }
                break;
        }

        return tab.stream().filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aCommands: "));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa send <style> <material> <player> <message>"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa reload"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa toggle"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa edit"));
    }
}