package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.editor.GUIHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ConfirmationGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();

    public static void open(Player player, String itemName) {
        Inventory gui = Bukkit.createInventory(null, 27, "Confirm Deletion");

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Are you sure?");
        infoMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Delete '" + itemName + "'?"));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(13, infoItem);

        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirmItem.setItemMeta(confirmMeta);
        gui.setItem(11, confirmItem);

        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(15, cancelItem);

        GUIHandler.fillBackground(gui);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 11) {
            Runnable action = GUIHandler.confirmationActions.remove(player.getUniqueId());
            if (action != null) {
                action.run();
            }
        } else if (slot == 15) {
            GUIHandler.confirmationActions.remove(player.getUniqueId());
            String rawLore = event.getInventory().getItem(13).getItemMeta().getLore().get(0);
            String itemName = ChatColor.stripColor(rawLore).replace("Delete '", "").replace("'?", "");
            if (plugin.getConfig().isSet("presets." + itemName)) {
                PresetsGUI.open(player);
            } else {
                AutoAnnounceGUI.open(player);
            }
        }
    }
}