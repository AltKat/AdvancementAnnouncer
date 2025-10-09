package io.github.altkat.advancementannouncer.Handlers.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

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
}