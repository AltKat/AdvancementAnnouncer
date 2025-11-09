package io.github.altkat.advancementannouncer.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JoinFeaturesGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Join Features");

        ItemStack joinMsgItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta joinMsgMeta = joinMsgItem.getItemMeta();
        joinMsgMeta.setDisplayName(ChatColor.YELLOW + "Normal Join Messages");
        joinMsgMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Edit messages shown when a player joins."));
        joinMsgItem.setItemMeta(joinMsgMeta);
        gui.setItem(11, joinMsgItem);

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "If you add multiple messages");
        infoLore.add(ChatColor.GRAY + "to a category, one will be");
        infoLore.add(ChatColor.GRAY + "selected randomly each time.");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(13, infoItem);

        ItemStack firstJoinMsgItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta firstJoinMsgMeta = firstJoinMsgItem.getItemMeta();
        firstJoinMsgMeta.setDisplayName(ChatColor.GOLD + "First Join Messages");
        firstJoinMsgMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Edit messages shown on first join."));
        firstJoinMsgItem.setItemMeta(firstJoinMsgMeta);
        gui.setItem(15, firstJoinMsgItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backItem.setItemMeta(backMeta);
        gui.setItem(26, backItem);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 11) {
            JoinMessageListGUI.open(player, "join");
        } else if (slot == 15) {
            JoinMessageListGUI.open(player, "first-join");
        } else if (slot == 26) {
            MainMenuGUI.open(player);
        }
    }
}