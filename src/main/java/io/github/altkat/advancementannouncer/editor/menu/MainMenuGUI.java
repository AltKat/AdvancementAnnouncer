package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.editor.GUIHandler;
import io.github.altkat.advancementannouncer.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenuGUI {
    private static final int SLOT_EDIT_AUTO_ANNOUNCE = 11;
    private static final int SLOT_JOIN_FEATURES = 13;
    private static final int SLOT_EDIT_PRESETS = 15;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TextUtil.color("&#7688FFAdvancement Announcer Edit"));

        ItemStack autoAnnounceItem = new ItemStack(Material.CLOCK);
        ItemMeta autoAnnounceMeta = autoAnnounceItem.getItemMeta();
        autoAnnounceMeta.setDisplayName(TextUtil.color("&bEdit Auto Announce"));
        autoAnnounceItem.setItemMeta(autoAnnounceMeta);
        gui.setItem(SLOT_EDIT_AUTO_ANNOUNCE, autoAnnounceItem);

        ItemStack joinFeaturesItem = new ItemStack(Material.IRON_DOOR);
        ItemMeta joinFeaturesMeta = joinFeaturesItem.getItemMeta();
        joinFeaturesMeta.setDisplayName(TextUtil.color("&6Edit Join Features"));
        joinFeaturesItem.setItemMeta(joinFeaturesMeta);
        gui.setItem(SLOT_JOIN_FEATURES, joinFeaturesItem);

        ItemStack presetsItem = new ItemStack(Material.PAPER);
        ItemMeta presetsMeta = presetsItem.getItemMeta();
        presetsMeta.setDisplayName(TextUtil.color("&#76FF90Edit Presets"));
        presetsItem.setItemMeta(presetsMeta);
        gui.setItem(SLOT_EDIT_PRESETS, presetsItem);

        GUIHandler.fillBackground(gui);

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