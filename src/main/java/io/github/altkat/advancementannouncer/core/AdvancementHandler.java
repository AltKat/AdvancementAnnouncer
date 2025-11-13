package io.github.altkat.advancementannouncer.core;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.cmd.ResolvedIconData;
import io.github.altkat.advancementannouncer.util.TextUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the logic of displaying advancements.
 * Will dynamically choose between UltimateAdvancementAPI (if available and enabled)
 * or the legacy Bukkit.getUnsafe() method (if API is missing or CMD is not used).
 */
public class AdvancementHandler {

    /**
     * The primary static method to display a toast.
     * It accepts a customModelData string and decides which backend to use.
     *
     * @param player Player to show the toast to.
     * @param iconMaterial The base material (e.g., "DIAMOND_SWORD").
     * @param customModelDataInput The CMD string (e.g., "12345", "itemsadder:my_item", or null/empty).
     * @param message The message to display.
     * @param style The frame style (GOAL, TASK, CHALLENGE).
     */
    public static void displayTo(Player player, String iconMaterial, String customModelDataInput, String message, Style style, String sound) {
        AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
        int cmdValue = 0;

        if (iconMaterial == null || iconMaterial.isBlank() || Material.matchMaterial(iconMaterial.toUpperCase()) == null) {
            iconMaterial = "PAPER";
        }

        if (customModelDataInput != null && !customModelDataInput.isBlank()) {
            ResolvedIconData data = plugin.getCmdResolver().resolve(customModelDataInput, iconMaterial);
            if (data != null) {
                cmdValue = data.getValue();
            } else {
                AdvancementAnnouncer.log("&eCould not resolve custom-model-data: '" + customModelDataInput + "' for icon '" + iconMaterial + "'. Using 0.");
            }
        }

        String finalMessage = TextUtil.color(message);
        if (plugin.isPAPIEnabled()) {
            finalMessage = PlaceholderAPI.setPlaceholders(player, finalMessage);
        }

        if (plugin.isApiAvailable()) {
            try {
                ItemStack iconStack = new ItemStack(Material.matchMaterial(iconMaterial.toUpperCase()));
                ItemMeta meta = iconStack.getItemMeta();
                if (meta != null) {
                    if (cmdValue > 0) {
                        meta.setCustomModelData(cmdValue);
                    }
                    iconStack.setItemMeta(meta);
                }

                UltimateAdvancementAPI api = plugin.getAdvancementAPI();
                AdvancementFrameType frameType = AdvancementFrameType.valueOf(style.name());

                api.displayCustomToast(
                        player,
                        iconStack,
                        finalMessage.replace("|", "\n"),
                        frameType
                );
            } catch (Exception e) {
                AdvancementAnnouncer.log("&cFailed to display toast using UltimateAdvancementAPI: " + e.getMessage());
                LegacyAdvancementHandler.displayTo(player, iconMaterial, finalMessage, style);
            }
        } else {
            LegacyAdvancementHandler.displayTo(player, iconMaterial, finalMessage, style);
        }
        playSound(player, sound);
    }

    /**
     * Overloaded method for legacy calls (from original code).
     */
    public static void displayTo(Player player, String icon, String message, Style style) {
        displayTo(player, icon, null, message, style, null);
    }

    private static void playSound(Player player, String soundKey) {
        if (soundKey == null || soundKey.isBlank()) {
            return;
        }

        if (!PlayerData.areSoundsEnabled(player.getUniqueId())) {
            return;
        }

        String finalSoundKey = soundKey.toLowerCase().trim().replace('_', '.');

        try {
            player.playSound(player.getLocation(), finalSoundKey, 1.0F, 1.0F);
        } catch (Exception e) {
            AdvancementAnnouncer.log("&c[Sound Error] An unexpected error occurred while trying to play sound '" + finalSoundKey + "': " + e.getMessage());
        }
    }

    public static enum Style {
        GOAL,
        TASK,
        CHALLENGE
    }


    /**
     * This private inner class holds the original, legacy logic for sending
     * advancements using Bukkit.getUnsafe().
     */
    private static class LegacyAdvancementHandler {
        private final NamespacedKey key;
        private final String icon;
        private final String message;
        private final Style style;
        private final AdvancementAnnouncer plugin;
        private static final Map<String, NamespacedKey> cachedKeys = new HashMap<>();

        private LegacyAdvancementHandler(String icon, String message, Style style) {
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
            String itemKey = plugin.isModernVersion() ? "id" : "item";

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
                AdvancementAnnouncer.log("&cError creating legacy advancement: " + e.getMessage());
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

        /**
         * The entry point for the legacy handler.
         */
        public static void displayTo(Player player, String icon, String message, Style style) {
            new LegacyAdvancementHandler(icon, message, style).start(player);
        }
    }
}