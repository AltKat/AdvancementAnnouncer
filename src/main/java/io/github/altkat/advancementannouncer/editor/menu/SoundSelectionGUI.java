package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.editor.ChatInputListener;
import io.github.altkat.advancementannouncer.editor.GUIHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SoundSelectionGUI {

    private static final Object[][] commonSounds = {
            {Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Level Up", "ENTITY_PLAYER_LEVELUP"},
            {Material.LAPIS_LAZULI, ChatColor.AQUA + "Orb Pickup", "ENTITY_EXPERIENCE_ORB_PICKUP"},
            {Material.EMERALD, ChatColor.GREEN + "Villager Yes", "ENTITY_VILLAGER_YES"},
            {Material.REDSTONE, ChatColor.RED + "Villager No", "ENTITY_VILLAGER_NO"},
            {Material.ANVIL, ChatColor.GRAY + "Anvil Land", "BLOCK_ANVIL_LAND"},
            {Material.TNT, ChatColor.RED + "Explosion", "ENTITY_GENERIC_EXPLODE"},
            {Material.BARRIER, ChatColor.RED + "No Sound", ""}
    };

    private static final int SLOT_CHAT_INPUT = 22;
    private static final int SLOT_INFO_BUTTON = 23;
    private static final int SLOT_BACK_BUTTON = 26;
    private static final String BUKKIT_SOUND_URL = "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Select a Sound");

        for (int i = 0; i < commonSounds.length; i++) {
            if (i + 10 >= 27) break;

            Material material = (Material) commonSounds[i][0];
            String displayName = (String) commonSounds[i][1];
            String soundName = (String) commonSounds[i][2];

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + (soundName.isEmpty() ? "None" : soundName));
            lore.add(" ");
            lore.add(ChatColor.GREEN + "Left-click to select.");
            lore.add(ChatColor.AQUA + "Right-click to preview.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(10 + i, item);
        }

        ItemStack chatItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta chatMeta = chatItem.getItemMeta();
        chatMeta.setDisplayName(ChatColor.AQUA + "Input via Chat");
        chatMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to type the sound", ChatColor.GRAY + "name in chat."));
        chatItem.setItemMeta(chatMeta);
        gui.setItem(SLOT_CHAT_INPUT, chatItem);

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Information");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Adding sounds is recommended for 'TASK' or 'GOAL' styles.",
                ChatColor.GRAY + "Since 'CHALLENGE' style has its own sound.",
                " ",
                ChatColor.GRAY + "This menu only shows common sounds.",
                ChatColor.GRAY + "Use 'Input via Chat' for any other sound.",
                " ",
                ChatColor.GRAY + "You can use custom sounds from",
                ChatColor.GRAY + "a resource pack (e.g. 'my.sound.effect')",
                " ",
                ChatColor.YELLOW + "Click for a full list of sounds."
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(SLOT_INFO_BUTTON, infoItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backItem.setItemMeta(backMeta);
        gui.setItem(SLOT_BACK_BUTTON, backItem);

        GUIHandler.fillBackground(gui);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        Map<String, Object> data = ChatInputListener.activeSessions.get(player.getUniqueId());
        if (data == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();

        if (slot == SLOT_BACK_BUTTON) {
            EditorGUI.open(player, data);
            return;
        }

        if (slot == SLOT_INFO_BUTTON) {
            player.closeInventory();

            TextComponent linkMessage = new TextComponent(ChatColor.GREEN + "[Click here for a full list of Bukkit sounds]");
            linkMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, BUKKIT_SOUND_URL));

            player.spigot().sendMessage(linkMessage);
            return;
        }

        if (slot == SLOT_CHAT_INPUT) {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Please type the new sound name.");
            player.sendMessage(ChatColor.GRAY + "(e.g., 'ENTITY_PLAYER_LEVELUP', or 'none' to clear)");
            player.sendMessage(ChatColor.GRAY + "(A list can be found on the Spigot Javadocs for 'Sound')");
            player.sendMessage(ChatColor.GRAY + "Current value: " + data.get("sound"));
            data.put("step", ChatInputListener.STEP_SOUND);
            return;
        }

        if (slot >= 10 && slot < 20 && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore()) {

            String soundName = ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0));
            if (soundName.equals("None")) {
                soundName = "";
            }

            if (event.isLeftClick()) {
                data.put("sound", soundName);
                player.sendMessage(ChatColor.GREEN + "Sound set to: " + (soundName.isEmpty() ? "None" : soundName));
                EditorGUI.open(player, data);

            } else if (event.isRightClick()) {
                if (!soundName.isEmpty()) {
                    try {
                        player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0F, 1.0F);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Could not preview sound: " + e.getMessage());
                    }
                }
            }
        }
    }
}