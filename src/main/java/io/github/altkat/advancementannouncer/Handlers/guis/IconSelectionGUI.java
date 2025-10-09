package io.github.altkat.advancementannouncer.Handlers.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IconSelectionGUI {

    private static final List<Material> materials = Arrays.stream(Material.values())
            .filter(material -> material.isItem() && material != Material.AIR)
            .collect(Collectors.toList());

    public static void open(Player player, int page) {
        int totalPages = (int) Math.ceil(materials.size() / 45.0);
        Inventory gui = Bukkit.createInventory(null, 54, "Select an Icon (Page " + (page + 1) + "/" + totalPages + ")");

        int startIndex = page * 45;
        for (int i = 0; i < 45; i++) {
            int materialIndex = startIndex + i;
            if (materialIndex < materials.size()) {
                Material material = materials.get(materialIndex);
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + material.name());
                    item.setItemMeta(meta);
                    gui.setItem(i, item);
                }

            } else {
                break;
            }
        }


        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prevPage.setItemMeta(prevMeta);
            gui.setItem(45, prevPage);
        }

        if (page < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextPage.setItemMeta(nextMeta);
            gui.setItem(53, nextPage);
        }


        ItemStack chatItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta chatMeta = chatItem.getItemMeta();
        chatMeta.setDisplayName(ChatColor.AQUA + "Input via Chat");
        chatMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to type the material", ChatColor.GRAY + "name in chat."));
        chatItem.setItemMeta(chatMeta);
        gui.setItem(48, chatItem);


        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }
}