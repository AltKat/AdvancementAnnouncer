package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdvancementHandler {
    private final NamespacedKey key;
    private final String icon;
    private final String message;
    private final Style style;
    private AdvancementAnnouncer plugin;

    private static final Map<String, NamespacedKey> cachedAdvancements = new HashMap<>();

    private AdvancementHandler(String icon, String message, Style style) {
        this.plugin = AdvancementAnnouncer.getInstance();
        this.icon = icon;
        this.message = message;
        this.style = style;

        String cacheKey = icon.toLowerCase() + "_" + style.toString().toLowerCase();

        if (!cachedAdvancements.containsKey(cacheKey)) {
            this.key = new NamespacedKey(plugin, "toast_" + cacheKey);
            createAdvancement();
            cachedAdvancements.put(cacheKey, this.key);
        } else {
            this.key = cachedAdvancements.get(cacheKey);
        }
    }

    private void start(Player player){
        grantAdvancement(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            revokeAdvancement(player);
        }, 10);
    }

    private void createAdvancement() {
        String itemKey;
        int version = plugin.getVersion();
        if (version <= 20) {
            itemKey = "item";
        }else {
            itemKey = "id";
        }

        String advancementJson = "{\n" +
                "    \"criteria\": {\n" +
                "        \"trigger\": {\n" +
                "            \"trigger\": \"minecraft:impossible\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"display\": {\n" +
                "        \"icon\": {\n" +
                "            \"" + itemKey + "\": \"minecraft:" + icon + "\"\n" +
                "        },\n" +
                "        \"title\": {\n" +
                "            \"text\": \"" + message.replace("|", "\n") + "\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "            \"text\": \"\"\n" +
                "        },\n" +
                "        \"background\": \"minecraft:textures/gui/advancements/backgrounds/adventure.png\",\n" +
                "        \"frame\": \"" + style.toString().toLowerCase() + "\",\n" +
                "        \"announce_to_chat\": false,\n" +
                "        \"show_toast\": true,\n" +
                "        \"hidden\": true\n" +
                "    },\n" +
                "    \"requirements\": [\n" +
                "        [\n" +
                "            \"trigger\"\n" +
                "        ]\n" +
                "    ]\n" +
                "}";

        try {
            Bukkit.getUnsafe().loadAdvancement(key, advancementJson);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create advancement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void grantAdvancement(Player player) {
        try {
            Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null) {
                player.getAdvancementProgress(adv).awardCriteria("trigger");
            } else {
                plugin.getLogger().warning("Advancement not found for key: " + key);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error granting advancement to " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void revokeAdvancement(Player player) {
        try {
            Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null) {
                player.getAdvancementProgress(adv).revokeCriteria("trigger");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error revoking advancement from " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void displayTo(Player player, String icon, String message, Style style) {
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        if(AdvancementAnnouncer.getInstance().isPAPIEnabled()) {
            String parsedMessage = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, coloredMessage));
            new AdvancementHandler(icon, parsedMessage, style).start(player);
        }else {
            new AdvancementHandler(icon, coloredMessage, style).start(player);
        }
    }

    public static enum Style {
        GOAL,
        TASK,
        CHALLENGE
    }
}