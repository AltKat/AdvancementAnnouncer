package io.github.altkat.advancementannouncer.Handlers.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ConfirmationGUI {

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

        player.openInventory(gui);
    }
}