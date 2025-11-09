package io.github.altkat.advancementannouncer.guis;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenuGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    private static final int SLOT_EDIT_AUTO_ANNOUNCE = 11;
    private static final int SLOT_JOIN_FEATURES = 13;
    private static final int SLOT_EDIT_PRESETS = 15;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.edit-gui-title")));

        ItemStack autoAnnounceItem = new ItemStack(Material.CLOCK);
        ItemMeta autoAnnounceMeta = autoAnnounceItem.getItemMeta();
        autoAnnounceMeta.setDisplayName(ChatColor.AQUA + "Edit Auto Announce");
        autoAnnounceItem.setItemMeta(autoAnnounceMeta);
        gui.setItem(SLOT_EDIT_AUTO_ANNOUNCE, autoAnnounceItem);

        ItemStack joinFeaturesItem = new ItemStack(Material.IRON_DOOR);
        ItemMeta joinFeaturesMeta = joinFeaturesItem.getItemMeta();
        joinFeaturesMeta.setDisplayName(ChatColor.GOLD + "Edit Join Features");
        joinFeaturesItem.setItemMeta(joinFeaturesMeta);
        gui.setItem(SLOT_JOIN_FEATURES, joinFeaturesItem);

        ItemStack presetsItem = new ItemStack(Material.PAPER);
        ItemMeta presetsMeta = presetsItem.getItemMeta();
        presetsMeta.setDisplayName(ChatColor.GREEN + "Edit Presets");
        presetsItem.setItemMeta(presetsMeta);
        gui.setItem(SLOT_EDIT_PRESETS, presetsItem);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == SLOT_EDIT_AUTO_ANNOUNCE) {
            AutoAnnounceGUI.open(player);
        } else if (slot == SLOT_EDIT_PRESETS) {
            PresetsGUI.open(player);
        } else if (slot == SLOT_JOIN_FEATURES) {
            JoinFeaturesGUI.open(player);
        }
    }
}