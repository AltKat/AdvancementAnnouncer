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
    private final AdvancementAnnouncer plugin;
    private static final Map<String, NamespacedKey> cachedKeys = new HashMap<>();

    private AdvancementHandler(String icon, String message, Style style) {
        this.plugin = AdvancementAnnouncer.getInstance();
        this.icon = icon;
        this.message = message;
        this.style = style;

        String cacheId = icon.toLowerCase() + "_" + style.toString().toLowerCase() + "_" + Integer.toHexString(message.hashCode());

        if (!cachedKeys.containsKey(cacheId)) {
            this.key = new NamespacedKey(plugin, "t_" + cacheId);
            createAdvancement();
            cachedKeys.put(cacheId, this.key);
        } else {
            this.key = cachedKeys.get(cacheId);
        }
    }

    private void start(Player player) {
        revokeAdvancement(player);
        grantAdvancement(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                revokeAdvancement(player);
            }
        }, 40L);
    }

    @SuppressWarnings("deprecation")
    private void createAdvancement() {
        String itemKey = (plugin.getVersion() <= 20) ? "item" : "id";

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
            plugin.getLogger().warning("Error creating advancement: " + e.getMessage());
        }
    }

    private void grantAdvancement(Player player) {
        try {
            Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null) {
                player.getAdvancementProgress(adv).awardCriteria("trigger");
            }
        } catch (Exception e) {
            // ignored
        }
    }

    private void revokeAdvancement(Player player) {
        try {
            Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null && player.isOnline()) {
                player.getAdvancementProgress(adv).revokeCriteria("trigger");
            }
        } catch (Exception e) {
            // ignored
        }
    }

    public static void displayTo(Player player, String icon, String message, Style style) {
        String finalMessage = ChatColor.translateAlternateColorCodes('&', message);

        if (AdvancementAnnouncer.getInstance().isPAPIEnabled()) {
            finalMessage = PlaceholderAPI.setPlaceholders(player, finalMessage);
        }

        new AdvancementHandler(icon, finalMessage, style).start(player);
    }

    public static enum Style {
        GOAL,
        TASK,
        CHALLENGE
    }
}