package io.github.altkat.advancementannouncer.feature;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.core.PlayerData;
import io.github.altkat.advancementannouncer.core.AdvancementHandler;
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
    static int lastMessageIndex = -1;
    static int failedAttempts = 0;

    public static void startAutoAnnounce() {
        if(!plugin.getConfig().getBoolean("auto-announce.enabled")){
            return;
        }

        ConfigurationSection mainSection = plugin.getConfig().getConfigurationSection("auto-announce");
        ConfigurationSection subSection = mainSection.getConfigurationSection("messages");
        if (subSection == null) {
            AdvancementAnnouncer.log("&eNo messages section found in the configuration. AutoAnnounce will not start.");
            return;
        }
        String mode = mainSection.getString("mode");
        order = 0;
        failedAttempts = 0;
        lastMessageIndex = -1;
        Set<String> keySet = subSection.getKeys(false);
        List<String> keyList = keySet.stream().toList();
        int messageCount = keyList.size();

        if (messageCount == 0) {
            AdvancementAnnouncer.log("&eNo messages found in the configuration. AutoAnnounce will not start.");
            return;
        }

        taskID = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (failedAttempts >= 10) {
                if (failedAttempts == 10) {
                    AdvancementAnnouncer.log("&cFailed to find a valid message after 10 attempts. Stopping auto announcements... Please fix the issue in the config then reload the plugin.");
                    stopAutoAnnounce();
                    failedAttempts++;
                }
                return;
            }

            String key;
            if (mode.equalsIgnoreCase("random")) {
                if (messageCount > 1) {
                    do {
                        order = ThreadLocalRandom.current().nextInt(messageCount);
                    } while (order == lastMessageIndex);
                } else {
                    order = 0;
                }
                key = keyList.get(order);
                lastMessageIndex = order;
            } else {
                if (order >= messageCount) order = 0;
                key = keyList.get(order);
                order++;
            }

            ConfigurationSection messageSection = subSection.getConfigurationSection(key);
            if (messageSection == null) {
                AdvancementAnnouncer.log("&eInvalid message configuration for key: " + key + ". Skipping.");
                failedAttempts++;
                return;
            }

            String styleString = messageSection.getString("style", "GOAL").toUpperCase();
            AdvancementHandler.Style style;
            try {
                style = AdvancementHandler.Style.valueOf(styleString);
            } catch (IllegalArgumentException e) {
                AdvancementAnnouncer.log("&eInvalid style: " + styleString + ". Skipping announcement (" + key + ").");
                failedAttempts++;
                return;
            }

            String icon = messageSection.getString("icon", "PAPER").toLowerCase();
            if (Material.matchMaterial(icon) == null) {
                AdvancementAnnouncer.log("&eInvalid icon: " + icon + ". Skipping announcement (" + key + ").");
                failedAttempts++;
                return;
            }

            String message = messageSection.getString("message");
            String customModelData = messageSection.getString("custom-model-data", "");
            String soundString = messageSection.getString("sound", "");

            Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
            if(playerList.isEmpty()) return;

            if(failedAttempts > 0){
                AdvancementAnnouncer.log("&aValid message found after " + failedAttempts + " attempts. Continuing auto announcements.");
                failedAttempts = 0;
            }

            for(Player player : playerList) {
                if(!PlayerData.returnToggleData(player.getUniqueId())) continue;
                AdvancementHandler.displayTo(player, icon, customModelData, message, style, soundString);
            }

        }, 60L, mainSection.getInt("interval") * 20L).getTaskId();

    }

    public static void stopAutoAnnounce() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}