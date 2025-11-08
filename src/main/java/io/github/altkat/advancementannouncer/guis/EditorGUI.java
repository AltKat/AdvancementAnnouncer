package io.github.altkat.advancementannouncer.guis;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.Handlers.AutoAnnounce;
import io.github.altkat.advancementannouncer.Handlers.ChatInputListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditorGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    private static final int SLOT_SET_NAME = 10;
    private static final int SLOT_SET_MESSAGE = 13;
    private static final int SLOT_SET_STYLE = 16;
    private static final int SLOT_SET_ICON = 19;
    private static final int SLOT_SAVE = 49;
    private static final int SLOT_CANCEL = 45;

    public static void open(Player player, Map<String, Object> data) {
        boolean isCreator = (boolean) data.get("isCreator");
        String type = (String) data.get("type");
        String title = isCreator ? "Creating new " + type : "Editing " + type + ": " + data.get("name");

        Inventory gui = Bukkit.createInventory(null, 54, title);

        ChatInputListener.activeSessions.put(player.getUniqueId(), data);

        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nameItem.getItemMeta();
        nameMeta.setDisplayName(ChatColor.YELLOW + "Set Name");
        List<String> nameLore = new ArrayList<>();
        nameLore.add(ChatColor.GRAY + "Current: " + data.get("name"));
        nameLore.add(" ");
        nameLore.add(ChatColor.GREEN + "Click to change the name via chat.");
        nameMeta.setLore(nameLore);
        nameItem.setItemMeta(nameMeta);
        gui.setItem(SLOT_SET_NAME, nameItem);

        ItemStack messageItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta messageMeta = messageItem.getItemMeta();
        messageMeta.setDisplayName(ChatColor.AQUA + "Set Message");
        List<String> messageLore = new ArrayList<>();
        messageLore.add(ChatColor.GRAY + "Current: ");
        addFormattedMessage(messageLore, (String) data.get("message"));
        messageLore.add(" ");
        messageLore.add(ChatColor.GREEN + "Click to change the message via chat.");
        messageMeta.setLore(messageLore);
        messageItem.setItemMeta(messageMeta);
        gui.setItem(SLOT_SET_MESSAGE, messageItem);

        if (type.equals("auto-announce")) {
            ItemStack styleItem = new ItemStack(Material.PAINTING);
            ItemMeta styleMeta = styleItem.getItemMeta();
            styleMeta.setDisplayName(ChatColor.GOLD + "Set Style");
            List<String> styleLore = new ArrayList<>();
            styleLore.add(ChatColor.GRAY + "Current: " + data.get("style"));
            styleLore.add(" ");
            styleLore.add(ChatColor.GREEN + "Click to choose a style.");
            styleMeta.setLore(styleLore);
            styleItem.setItemMeta(styleMeta);
            gui.setItem(SLOT_SET_STYLE, styleItem);

            Material iconMaterial;
            try {
                iconMaterial = Material.valueOf(((String) data.get("icon")).toUpperCase());
            } catch (Exception e) {
                iconMaterial = Material.STONE;
            }
            ItemStack iconItem = new ItemStack(iconMaterial);
            ItemMeta iconMeta = iconItem.getItemMeta();
            iconMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Set Icon");
            List<String> iconLore = new ArrayList<>();
            iconLore.add(ChatColor.GRAY + "Current: " + data.get("icon"));
            iconLore.add(" ");
            iconLore.add(ChatColor.GREEN + "Click to choose an icon.");
            iconMeta.setLore(iconLore);
            iconItem.setItemMeta(iconMeta);
            gui.setItem(SLOT_SET_ICON, iconItem);
        }

        ItemStack saveItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save");
        saveItem.setItemMeta(saveMeta);
        gui.setItem(SLOT_SAVE, saveItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Cancel");
        backItem.setItemMeta(backMeta);
        gui.setItem(SLOT_CANCEL, backItem);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Map<String, Object> data = ChatInputListener.activeSessions.get(player.getUniqueId());

        if (data == null) {
            player.closeInventory();
            return;
        }

        switch (event.getSlot()) {
            case SLOT_SET_NAME:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please type the new name in chat. (Type 'cancel' to abort)");
                player.sendMessage(ChatColor.GRAY + "Current value: " + data.get("name"));
                data.put("step", ChatInputListener.STEP_NAME);
                break;
            case SLOT_SET_MESSAGE:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please type the new message in chat. (Use | for new line, type 'cancel' to abort)");
                player.sendMessage(ChatColor.GRAY + "Current value: " + data.get("message"));
                data.put("step", ChatInputListener.STEP_MESSAGE);
                break;
            case SLOT_SET_STYLE:
                StyleSelectionGUI.open(player);
                break;
            case SLOT_SET_ICON:
                IconSelectionGUI.open(player, 0);
                break;
            case SLOT_SAVE:
                saveChanges(player, data);
                break;
            case SLOT_CANCEL:
                ChatInputListener.activeSessions.remove(player.getUniqueId());
                if (((String) data.get("type")).equals("preset")) {
                    PresetsGUI.open(player);
                } else {
                    AutoAnnounceGUI.open(player);
                }
                break;
        }
    }

    private static void saveChanges(Player player, Map<String, Object> data) {
        String name = (String) data.get("name");

        if (name == null || name.isBlank() || name.equals("<not set>")) {
            player.sendMessage(ChatColor.RED + "You must set a name before saving!");
            return;
        }

        boolean isCreator = (boolean) data.get("isCreator");
        String type = (String) data.get("type");
        String originalName = (String) data.get("originalName");

        if (!isCreator && originalName != null && !originalName.equals(name)) {
            String oldPath = type.equals("preset") ? "presets." + originalName : "auto-announce.messages." + originalName;
            plugin.getConfig().set(oldPath, null);
        }

        String newPathCheck = type.equals("preset") ? "presets." : "auto-announce.messages.";
        if (plugin.getConfig().contains(newPathCheck + name) && (isCreator || !originalName.equals(name))) {
            player.sendMessage(ChatColor.RED + "An item with this name already exists!");
            return;
        }

        ChatInputListener.activeSessions.remove(player.getUniqueId());

        if (type.equals("preset")) {
            plugin.getConfig().set("presets." + name, data.get("message"));
        } else {
            String path = "auto-announce.messages." + name;
            plugin.getConfig().set(path + ".message", data.get("message"));
            plugin.getConfig().set(path + ".style", data.get("style"));
            plugin.getConfig().set(path + ".icon", data.get("icon"));
        }

        plugin.saveConfig();
        AutoAnnounce.stopAutoAnnounce();
        AutoAnnounce.startAutoAnnounce();
        player.sendMessage(ChatColor.GREEN + "Changes saved successfully!");

        if (type.equals("preset")) {
            PresetsGUI.open(player);
        } else {
            AutoAnnounceGUI.open(player);
        }
    }

    private static void addFormattedMessage(List<String> lore, String message) {
        if (message != null && message.contains("|")) {
            for (String line : message.split("\\|")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        } else if (message != null) {
            lore.add(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}