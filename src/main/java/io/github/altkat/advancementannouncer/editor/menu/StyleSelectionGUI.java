package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.editor.ChatInputListener;
import io.github.altkat.advancementannouncer.editor.GUIHandler;
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
import java.util.Collections;
import java.util.Map;

public class StyleSelectionGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Select a Style");

        ItemStack goalItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta goalMeta = goalItem.getItemMeta();
        goalMeta.setDisplayName(TextUtil.color("&#76FF90GOAL"));
        goalMeta.setLore(Collections.singletonList(TextUtil.color("&7Select the 'GOAL' style.")));
        goalItem.setItemMeta(goalMeta);
        gui.setItem(10, goalItem);

        ItemStack taskItem = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta taskMeta = taskItem.getItemMeta();
        taskMeta.setDisplayName(TextUtil.color("&#FCD05CTASK"));
        taskMeta.setLore(Collections.singletonList(TextUtil.color("&7Select the 'TASK' style.")));
        taskItem.setItemMeta(taskMeta);
        gui.setItem(13, taskItem);

        ItemStack challengeItem = new ItemStack(Material.RED_WOOL);
        ItemMeta challengeMeta = challengeItem.getItemMeta();
        challengeMeta.setDisplayName(TextUtil.color("&#F86B6BCHALLENGE"));
        challengeMeta.setLore(Collections.singletonList(TextUtil.color("&7Select the 'CHALLENGE' style.")));
        challengeItem.setItemMeta(challengeMeta);
        gui.setItem(16, challengeItem);

        ItemStack chatItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta chatMeta = chatItem.getItemMeta();
        chatMeta.setDisplayName(TextUtil.color("&bInput via Chat"));
        chatMeta.setLore(Arrays.asList(
                TextUtil.color("&7Click to type the style"),
                TextUtil.color("&7name in chat.")));
        chatItem.setItemMeta(chatMeta);
        gui.setItem(22, chatItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(TextUtil.color("&#F86B6BBack"));
        backItem.setItemMeta(backMeta);
        gui.setItem(26, backItem);

        GUIHandler.fillBackground(gui);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        if (clickedItem.getType() == Material.WRITABLE_BOOK) {
            player.closeInventory();
            player.sendMessage(TextUtil.color("&#76FF90Please type the style name (GOAL, TASK, CHALLENGE) in chat. &7(Type 'cancel' to abort)"));
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
                player.sendMessage(TextUtil.color("&#76FF90Style set to " + styleName + "!"));
                EditorGUI.open(player, data);
            }
        }
    }
}