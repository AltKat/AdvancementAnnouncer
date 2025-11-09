package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.guis.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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
}