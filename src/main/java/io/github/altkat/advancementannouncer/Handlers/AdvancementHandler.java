package io.github.altkat.advancementannouncer.Handlers;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

    // Note: All non-static fields and methods from the original file have been moved
    // into the private inner class 'LegacyAdvancementHandler' to keep the legacy code separate.

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
    public static void displayTo(Player player, String iconMaterial, String customModelDataInput, String message, Style style) {
        AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
        int cmdValue = 0;

        // 1. Validate the icon material
        if (iconMaterial == null || iconMaterial.isBlank() || Material.matchMaterial(iconMaterial.toUpperCase()) == null) {
            iconMaterial = "PAPER"; // A safe default
        }

        // 2. Resolve the CustomModelData string (if it exists)
        // This uses the CustomModelDataResolver class
        if (customModelDataInput != null && !customModelDataInput.isBlank()) {
            ResolvedIconData data = plugin.getCmdResolver().resolve(customModelDataInput, iconMaterial);
            if (data != null) {
                cmdValue = data.getValue();
            } else {
                plugin.getLogger().warning("Could not resolve custom-model-data: '" + customModelDataInput + "' for icon '" + iconMaterial + "'. Using 0.");
            }
        }

        // 3. Apply Placeholders and Colors
        String finalMessage = ChatColor.translateAlternateColorCodes('&', message);
        if (plugin.isPAPIEnabled()) {
            finalMessage = PlaceholderAPI.setPlaceholders(player, finalMessage);
        }

        // 4. Check if we should use the API
        // Use API if:
        // 1. It is enabled in config AND loaded on the server (isApiAvailable())
        // 2. We actually have a CustomModelData value to display (cmdValue > 0)
        if (plugin.isApiAvailable() && cmdValue > 0) {
            try {
                // --- METHOD 1: Use UltimateAdvancementAPI (Supports CMD) ---

                // Create the icon ItemStack with CustomModelData
                ItemStack iconStack = new ItemStack(Material.matchMaterial(iconMaterial.toUpperCase()));
                ItemMeta meta = iconStack.getItemMeta(); // We know meta is not null from 1.16.5+
                if (meta != null) {
                    meta.setCustomModelData(cmdValue);
                    iconStack.setItemMeta(meta);
                }

                UltimateAdvancementAPI api = plugin.getAdvancementAPI();
                // Convert our enum to the API's enum
                AdvancementFrameType frameType = AdvancementFrameType.valueOf(style.name());

                // Call the API's toast method
                api.displayCustomToast(
                        player,
                        iconStack,
                        finalMessage.replace("|", "\n"),
                        frameType
                );
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to display toast using UltimateAdvancementAPI: " + e.getMessage());
                // Fallback to legacy method if API fails
                LegacyAdvancementHandler.displayTo(player, iconMaterial, finalMessage, style);
            }
        } else {
            // --- METHOD 2: Use Legacy Bukkit.getUnsafe() (No CMD) ---
            // (Used if API is disabled, not installed, or cmdValue is 0)
            LegacyAdvancementHandler.displayTo(player, iconMaterial, finalMessage, style);
        }
    }

    /**
     * Overloaded method for legacy calls (from original code).
     * This now redirects to the new main method with a 'null' customModelDataInput.
     */
    public static void displayTo(Player player, String icon, String message, Style style) {
        // Call the new main method with null CMD
        displayTo(player, icon, null, message, style);
    }

    /**
     * Enum for the toast frame type.
     */
    public static enum Style {
        GOAL,
        TASK,
        CHALLENGE
    }


    // --- LEGACY HANDLER INNER CLASS ---

    /**
     * This private inner class holds the original, legacy logic from AdvancementHandler.java
     * for sending advancements using Bukkit.getUnsafe().
     * This logic does NOT support CustomModelData and is used as a fallback.
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

            // Legacy cache ID, does not include CMD
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
            // FIXED: Use the new isModernVersion() check from the main class
            // to correctly handle 1.20.5+ ('id') vs pre-1.20.5 ('item')
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
                plugin.getLogger().warning("Error creating legacy advancement: " + e.getMessage());
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
            // Message is already color-translated and PAPI-parsed by the main displayTo method
            new LegacyAdvancementHandler(icon, message, style).start(player);
        }
    }
}