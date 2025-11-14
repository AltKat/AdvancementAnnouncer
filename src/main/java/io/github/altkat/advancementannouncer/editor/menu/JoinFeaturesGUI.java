package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.editor.GUIHandler;
import io.github.altkat.advancementannouncer.util.TextUtil;
import org.bukkit.Bukkit;
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
        Inventory gui = Bukkit.createInventory(null, 27, TextUtil.color("&#7688FFJoin Features"));

        ItemStack joinMsgItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta joinMsgMeta = joinMsgItem.getItemMeta();
        joinMsgMeta.setDisplayName(TextUtil.color("&#FCD05CNormal Join Messages"));
        joinMsgMeta.setLore(Collections.singletonList(TextUtil.color("&7Edit messages shown when a player joins.")));
        joinMsgItem.setItemMeta(joinMsgMeta);
        gui.setItem(11, joinMsgItem);

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(TextUtil.color("&bInformation"));
        List<String> infoLore = new ArrayList<>();
        infoLore.add(TextUtil.color("&7If you add multiple messages"));
        infoLore.add(TextUtil.color("&7to a category, one will be"));
        infoLore.add(TextUtil.color("&7selected randomly each time."));
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(13, infoItem);

        ItemStack firstJoinMsgItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta firstJoinMsgMeta = firstJoinMsgItem.getItemMeta();
        firstJoinMsgMeta.setDisplayName(TextUtil.color("&6First Join Messages"));
        firstJoinMsgMeta.setLore(Collections.singletonList(TextUtil.color("&7Edit messages shown on first join.")));
        firstJoinMsgItem.setItemMeta(firstJoinMsgMeta);
        gui.setItem(15, firstJoinMsgItem);

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