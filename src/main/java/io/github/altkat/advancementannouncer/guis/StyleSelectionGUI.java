package io.github.altkat.advancementannouncer.guis;

import io.github.altkat.advancementannouncer.Handlers.ChatInputListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class StyleSelectionGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Select a Style");

        ItemStack goalItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta goalMeta = goalItem.getItemMeta();
        goalMeta.setDisplayName(ChatColor.GREEN + "GOAL");
        goalMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Select the 'GOAL' style."));
        goalItem.setItemMeta(goalMeta);
        gui.setItem(10, goalItem);

        ItemStack taskItem = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta taskMeta = taskItem.getItemMeta();
        taskMeta.setDisplayName(ChatColor.YELLOW + "TASK");
        taskMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Select the 'TASK' style."));
        taskItem.setItemMeta(taskMeta);
        gui.setItem(13, taskItem);

        ItemStack challengeItem = new ItemStack(Material.RED_WOOL);
        ItemMeta challengeMeta = challengeItem.getItemMeta();
        challengeMeta.setDisplayName(ChatColor.RED + "CHALLENGE");
        challengeMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Select the 'CHALLENGE' style."));
        challengeItem.setItemMeta(challengeMeta);
        gui.setItem(16, challengeItem);

        ItemStack chatItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta chatMeta = chatItem.getItemMeta();
        chatMeta.setDisplayName(ChatColor.AQUA + "Input via Chat");
        chatMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to type the style", ChatColor.GRAY + "name in chat."));
        chatItem.setItemMeta(chatMeta);
        gui.setItem(22, chatItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backItem.setItemMeta(backMeta);
        gui.setItem(26, backItem);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        if (clickedItem.getType() == Material.WRITABLE_BOOK) {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Please type the style name (GOAL, TASK, CHALLENGE) in chat. (Type 'cancel' to abort)");
            ChatInputListener.activeSessions.get(player.getUniqueId()).put("step", ChatInputListener.STEP_STYLE);
            return;
        }

        if (clickedItem.getType() == Material.BARRIER) {
            Map<String, Object> data = ChatInputListener.activeSessions.get(player.getUniqueId());
            if (data != null) {
                EditorGUI.open(player, data);
            }
            return;
        }

        if (clickedItem.getType().name().endsWith("_WOOL")) {
            String styleName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            Map<String, Object> data = ChatInputListener.activeSessions.get(player.getUniqueId());
            if (data != null) {
                data.put("style", styleName);
                player.sendMessage(ChatColor.GREEN + "Style set to " + styleName + "!");
                EditorGUI.open(player, data);
            }
        }
    }
}