package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.editor.ChatInputListener;
import io.github.altkat.advancementannouncer.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
                    meta.setDisplayName(TextUtil.color("&#76FF90" + material.name()));
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
            prevMeta.setDisplayName(TextUtil.color("&#FCD05CPrevious Page"));
            prevPage.setItemMeta(prevMeta);
            gui.setItem(45, prevPage);
        }

        if (page < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(TextUtil.color("&#FCD05CNext Page"));
            nextPage.setItemMeta(nextMeta);
            gui.setItem(53, nextPage);
        }

        ItemStack chatItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta chatMeta = chatItem.getItemMeta();
        chatMeta.setDisplayName(TextUtil.color("&bInput via Chat"));
        chatMeta.setLore(Arrays.asList(
                TextUtil.color("&7Click to type the material"),
                TextUtil.color("&7name in chat.")));
        chatItem.setItemMeta(chatMeta);
        gui.setItem(48, chatItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(TextUtil.color("&#F86B6BBack"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        String title = event.getView().getTitle();

        if (clickedItem == null) return;

        if (clickedItem.getType() == Material.WRITABLE_BOOK) {
            player.closeInventory();
            player.sendMessage(TextUtil.color("&#76FF90Please type the material name for the icon in chat. &7(Type 'cancel' to abort)"));
            ChatInputListener.activeSessions.get(player.getUniqueId()).put("step", ChatInputListener.STEP_ICON);
            return;
        }

        if (clickedItem.getType() == Material.BARRIER) {
            Map<String, Object> data = ChatInputListener.activeSessions.get(player.getUniqueId());
            if (data != null) {
                EditorGUI.open(player, data);
            }
            return;
        }

        if (clickedItem.getType() == Material.ARROW) {
            String currentPageStr = title.substring(title.indexOf("Page ") + 5, title.indexOf("/"));
            int currentPage = Integer.parseInt(currentPageStr) - 1;
            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (itemName.equals("Next Page")) {
                open(player, currentPage + 1);
            } else if (itemName.equals("Previous Page")) {
                open(player, currentPage - 1);
            }
            return;
        }

        Material selectedMaterial = clickedItem.getType();
        Map<String, Object> data = ChatInputListener.activeSessions.get(player.getUniqueId());
        if (data != null) {
            data.put("icon", selectedMaterial.name());
            player.sendMessage(TextUtil.color("&#76FF90Icon set to " + selectedMaterial.name() + "&#76FF90!"));
            EditorGUI.open(player, data);
        }
    }
}