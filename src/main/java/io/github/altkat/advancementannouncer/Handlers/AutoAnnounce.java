package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class AutoAnnounce {
    static AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    static int taskID;

    static int order = 0;
    static int lastMessageIndex;
    static int failedAttempts = 0;
    public static void startAutoAnnounce() {
        if(plugin.getConfig().getConfigurationSection("auto-announce").getBoolean("enabled")){
            ConfigurationSection mainSection = plugin.getConfig().getConfigurationSection("auto-announce");
            ConfigurationSection subSection = mainSection.getConfigurationSection("messages");
            if (subSection == null) {
                plugin.getLogger().warning("No messages section found in the configuration. AutoAnnounce will not start.");
                return;
            }
            String mode = mainSection.getString("mode");
            order = 0;
            failedAttempts = 0;
            Set<String> keySet = subSection.getKeys(false);
            List<String> keyList = keySet.stream().toList();
            int messageCount = keyList.size();

            if (messageCount == 0) {
                plugin.getLogger().warning("No messages found in the configuration. AutoAnnounce will not start.");
                return;
            }
            if(mode.equalsIgnoreCase("random")) {
                taskID = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if(failedAttempts == 10){
                        plugin.getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §cFailed to find a valid message after 10 attempts. Stopping auto announcements... Please fix the issue in the config then reload the plugin.");
                        stopAutoAnnounce();
                        return;
                    }
                    do {
                        order = ThreadLocalRandom.current().nextInt(messageCount);
                    } while (order == lastMessageIndex);
                    String message = subSection.getConfigurationSection(keyList.get(order)).getString("message");
                    String styleString = subSection.getConfigurationSection(keyList.get(order)).getString("style").toUpperCase();
                    AdvancementHandler.Style style;
                    try {
                        style = AdvancementHandler.Style.valueOf(styleString);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid style: " + styleString + ". Skipping announcement (" + keyList.get(order) + "). Remaining attempts before disabling auto announcements: " + (10 - failedAttempts));
                        failedAttempts++;
                        return;
                    }
                    String icon = subSection.getConfigurationSection(keyList.get(order)).getString("icon").toLowerCase();
                    if (Material.matchMaterial(icon) == null) {
                        plugin.getLogger().warning("Invalid icon: " + icon + ". Skipping announcement (" + keyList.get(order) + "). Remaining attempts before disabling auto announcements: " + (10 - failedAttempts));
                        failedAttempts++;
                        return;
                    }
                    Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
                    if(playerList.isEmpty()) return;
                    for(Player player : playerList) {

                        if(failedAttempts > 0){
                            plugin.getLogger().warning("Valid message found after " + failedAttempts + " attempts. Continuing auto announcements.");
                            failedAttempts = 0;
                        }

                        if(!PlayerData.returnToggleData(player.getUniqueId())) continue;
                        AdvancementHandler.displayTo(player, icon, message, style);
                    }
                    lastMessageIndex = order;
                }, 60L, mainSection.getInt("interval") * 20L).getTaskId();

            } else {
                taskID = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if(order >= messageCount) order = 0;
                    String message = subSection.getConfigurationSection(keyList.get(order)).getString("message");
                    String styleString = subSection.getConfigurationSection(keyList.get(order)).getString("style").toUpperCase();
                    AdvancementHandler.Style style;
                    try {
                        style = AdvancementHandler.Style.valueOf(styleString);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid style: " + styleString + ". Skipping announcement (" + keyList.get(order) + ").");
                        order++;
                        return;
                    }
                    String icon = subSection.getConfigurationSection(keyList.get(order)).getString("icon").toLowerCase();
                    if (Material.matchMaterial(icon) == null) {
                        plugin.getLogger().warning("Invalid icon: " + icon + ". Skipping announcement (" + keyList.get(order) + ").");
                        order++;
                        return;
                    }
                    Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
                    if(playerList.isEmpty()) return;
                    for(Player player : playerList) {
                        if(!PlayerData.returnToggleData(player.getUniqueId())) continue;
                        AdvancementHandler.displayTo(player, icon, message, style);
                    }
                    order++;
                }, 60L, mainSection.getInt("interval") * 20L).getTaskId();
            }
        }
    }

    public static void stopAutoAnnounce() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}