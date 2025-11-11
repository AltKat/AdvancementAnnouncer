package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.guis.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIHandler implements Listener {
    private final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    public static final Map<UUID, Runnable> confirmationActions = new HashMap<>();
    private final Set<UUID> isNavigating = new HashSet<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        if (!isRelevantGUI(title)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        if (isNavigating.contains(player.getUniqueId())) {
            return;
        }
        isNavigating.add(player.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> isNavigating.remove(player.getUniqueId()), 5L);

        if (title.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.edit-gui-title")))) {
            MainMenuGUI.handleClick(event);
        } else if (title.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title")))) {
            PresetsGUI.handleClick(event);
        } else if (title.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.auto-announce-gui-title")))) {
            AutoAnnounceGUI.handleClick(event);
        } else if (title.contains("Join Features") || title.contains("Join Messages")) {
            if (title.contains("Join Features")) {
                JoinFeaturesGUI.handleClick(event);
            } else {
                JoinMessageListGUI.handleClick(event);
            }
        } else if (title.startsWith("Editing ") || title.startsWith("Creating ")) {
            EditorGUI.handleClick(event);
        } else if (title.equals("Select a Style")) {
            StyleSelectionGUI.handleClick(event);
        } else if (title.startsWith("Select an Icon")) {
            IconSelectionGUI.handleClick(event);
        } else if (title.equals("Confirm Deletion")) {
            ConfirmationGUI.handleClick(event);
        }
    }

    private boolean isRelevantGUI(String title) {
        return title.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.edit-gui-title"))) ||
                title.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title"))) ||
                title.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.auto-announce-gui-title"))) ||
                title.contains("Join Features") ||
                title.contains("Join Messages") ||
                title.startsWith("Editing ") ||
                title.startsWith("Creating ") ||
                title.equals("Select a Style") ||
                title.startsWith("Select an Icon") ||
                title.equals("Confirm Deletion");
    }

    /**
     * Creates an ItemStack for a GUI, applying CustomModelData if available and enabled.
     * Falls back to the base material if CMD fails or is disabled.
     *
     * @param icon     The base material name (e.g., "DIAMOND")
     * @param cmdInput The CustomModelData string (e.g., "12345" or "itemsadder:my_item")
     * @param displayName The display name for the item
     * @param lore     The lore for the item
     * @return An ItemStack, potentially with CustomModelData.
     */
    public static ItemStack createDisplayItem(String icon, String cmdInput, String displayName, List<String> lore) {
        AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
        Material material;

        try {
            material = Material.valueOf(icon.toUpperCase());
        } catch (Exception e) {
            material = Material.PAPER;
        }

        ItemStack item = new ItemStack(material);
        int cmdValue = 0;

        if (plugin.isApiAvailable() && cmdInput != null && !cmdInput.isBlank()) {
            ResolvedIconData data = plugin.getCmdResolver().resolve(cmdInput, icon);
            if (data != null) {
                cmdValue = data.getValue();
            }
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);

            if (cmdValue > 0) {
                try {
                    meta.setCustomModelData(cmdValue);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to apply CustomModelData to GUI icon: " + e.getMessage());
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}