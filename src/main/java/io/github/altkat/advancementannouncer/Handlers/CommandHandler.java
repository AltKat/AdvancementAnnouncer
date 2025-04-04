package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {
    AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!sender.hasPermission("advancementannouncer.admin")){
            if(args.length != 1 || !args[0].equalsIgnoreCase("toggle")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.wrong-usage")));
                return true;
            }else{
                UUID playerUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
                PlayerData.setToggleData(playerUUID, !PlayerData.returnToggleData(playerUUID));
                if(PlayerData.returnToggleData(playerUUID)){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-on")));
                }else{
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-off")));
                }
            }
            return true;
        }

        if(args.length == 0){
            sendHelpMessage(sender);
            return true;
        }

        if(args[0].equalsIgnoreCase("toggle")){
            if(!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                return true;
            }
            UUID playerUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
            PlayerData.setToggleData(playerUUID, !PlayerData.returnToggleData(playerUUID));
            if(PlayerData.returnToggleData(playerUUID)){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-on")));
            }else{
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-off")));
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("reload")){
            AutoAnnounce.stopAutoAnnounce();
            plugin.loadConfig();
            AutoAnnounce.startAutoAnnounce();
            PlayerData.reloadPlayerData();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.config-reloaded")));
            return true;
        }

        if (args.length < 4){
            sendHelpMessage(sender);
            return true;
        }

        final AdvancementHandler.Style style;

        try {
            style = AdvancementHandler.Style.valueOf(args[0].toUpperCase());

        } catch (final Throwable t) {
            sender.sendMessage(ChatColor.RED + "Invalid style: " + args[0]);

            return true;
        }

        final String materialName = args[1];

        try {
            Material.valueOf(materialName.toUpperCase());

        } catch (final Throwable t) {
            sender.sendMessage(ChatColor.RED + "Invalid material: " + materialName);

            return true;
        }

        String audience = args[2];

        String message = "";

        if(plugin.getConfig().getConfigurationSection("presets").getKeys(false).contains(args[3])) {
            message = plugin.getConfig().getString("presets." + args[3]);
        }else {

            for (int i = 3; i < args.length; i++)
                message += args[i] + " ";

        }
        message = ChatColor.translateAlternateColorCodes('&', message.trim());

        if(audience.equalsIgnoreCase("all")){
            if(sender.getServer().getOnlinePlayers().isEmpty()){
                sender.sendMessage(ChatColor.RED + "There are no online players in the server!");
                return true;
            }
            for(Player player : sender.getServer().getOnlinePlayers()){
                if(!PlayerData.returnToggleData(player.getUniqueId())){
                    continue;
                }
                AdvancementHandler.displayTo(player, materialName, message, style);
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to all players"));
        }else{
            Player player = sender.getServer().getPlayer(audience);
            if(player == null){
                sender.sendMessage(ChatColor.RED + "Player not found: " + audience);
                return true;
            }
            AdvancementHandler.displayTo(player, materialName, message, style);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to " + player.getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final List<String> tab = new ArrayList<>();
        if(!sender.hasPermission("advancementannouncer.admin")){
            if(args.length == 1){
                tab.add("toggle");
                return tab;
            }
            return tab;
        }

        switch (args.length) {
            case 1:
                tab.add("reload");
                tab.add("toggle");
                for (final AdvancementHandler.Style style : AdvancementHandler.Style.values())
                    tab.add(style.toString().toLowerCase());
                break;

            case 2:
                if(args[0].equalsIgnoreCase("goal") || args[0].equalsIgnoreCase("challenge") || args[0].equalsIgnoreCase("task")) {
                    for (final Material material : Material.values())
                        tab.add(material.toString().toLowerCase());
                }
                break;

            case 3:
                tab.add("all");
                for (final Player player : sender.getServer().getOnlinePlayers()) {
                    tab.add(player.getName());
                }
                break;
            case 4:
                tab.addAll(plugin.getConfig().getConfigurationSection("presets").getKeys(false));
                tab.add("your message");
                break;
        }

        return tab.stream().filter(completion -> completion.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

    private void sendHelpMessage(CommandSender sender){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aCommands: "));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa <style> <material> <player> <message>"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa reload"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa toggle"));
    }
}
