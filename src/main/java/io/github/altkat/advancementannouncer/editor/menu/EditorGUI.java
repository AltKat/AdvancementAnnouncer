package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.feature.AutoAnnounce;
import io.github.altkat.advancementannouncer.editor.ChatInputListener;
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
    private static final int SLOT_SET_CUSTOM_MODEL_DATA = 22;
    private static final int SLOT_SAVE = 49;
    private static final int SLOT_CANCEL = 45;

    public static void open(Player player, Map<String, Object> data) {
        boolean isCreator = (boolean) data.get("isCreator");
        String type = (String) data.get("type");
        String readableType = type.replace("-", " ");
        String title = (isCreator ? "Creating " : "Editing ") + readableType;
        if (title.length() > 32) title = title.substring(0, 32);

        Inventory gui = Bukkit.createInventory(null, 54, title);

        data.putIfAbsent("custom-model-data", "");

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

        ItemStack iconCmdItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta iconCmdMeta = iconCmdItem.getItemMeta();
        iconCmdMeta.setDisplayName(ChatColor.DARK_AQUA + "Set CustomModelData");
        List<String> iconCmdLore = new ArrayList<>();
        String currentCmd = data.get("custom-model-data").toString();
        iconCmdLore.add(ChatColor.GRAY + "Current: " + (currentCmd.isEmpty() ? "None" : currentCmd));
        iconCmdLore.add(" ");
        iconCmdLore.add(ChatColor.GREEN + "Click to change via chat.");
        iconCmdLore.add(ChatColor.GRAY + "(e.g., '12345', 'itemsadder:my_item',");
        iconCmdLore.add(ChatColor.GRAY + "'nexo:my_item', or 'none' to clear.)");
        iconCmdLore.add(" ");
        if (!plugin.isApiAvailable()) {
            iconCmdLore.add(ChatColor.RED + "WARNING: This feature is disabled.");
            iconCmdLore.add(ChatColor.RED + "Install 'UltimateAdvancementAPI' and set");
            iconCmdLore.add(ChatColor.RED + "'enable-custom-model-support' to true.");
        } else {
            iconCmdLore.add(ChatColor.GREEN + "Custom Model Support is ENABLED.");
        }
        iconCmdMeta.setLore(iconCmdLore);
        iconCmdItem.setItemMeta(iconCmdMeta);
        gui.setItem(SLOT_SET_CUSTOM_MODEL_DATA, iconCmdItem);

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
            case SLOT_SET_CUSTOM_MODEL_DATA:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please type the new CustomModelData value.");
                player.sendMessage(ChatColor.GRAY + "(e.g., '12345', 'itemsadder:my_item', 'nexo:my_item', or 'none' to clear)");
                player.sendMessage(ChatColor.GRAY + "Current value: " + data.get("custom-model-data"));
                data.put("step", ChatInputListener.STEP_CUSTOM_MODEL_DATA);
                break;
            case SLOT_SAVE:
                saveChanges(player, data);
                break;
            case SLOT_CANCEL:
                ChatInputListener.activeSessions.remove(player.getUniqueId());
                returnToPreviousMenu(player, (String) data.get("type"));
                break;
        }
    }

    private static void returnToPreviousMenu(Player player, String type) {
        switch (type) {
            case "preset":
                PresetsGUI.open(player);
                break;
            case "auto-announce":
                AutoAnnounceGUI.open(player);
                break;
            case "join-message":
                JoinMessageListGUI.open(player, "join");
                break;
            case "first-join-message":
                JoinMessageListGUI.open(player, "first-join");
                break;
            default:
                MainMenuGUI.open(player);
                break;
        }
    }

    private static void saveChanges(Player player, Map<String, Object> data) {
        final String prefix = plugin.getPrefix();
        String name = (String) data.get("name");

        if (name == null || name.isBlank() || name.equals("<not set>")) {
            player.sendMessage(prefix + ChatColor.RED + "You must set a name before saving!");
            return;
        }

        boolean isCreator = (boolean) data.get("isCreator");
        String type = (String) data.get("type");
        String originalName = (String) data.get("originalName");

        String basePath;
        switch (type) {
            case "preset":
                basePath = "presets.";
                break;
            case "auto-announce":
                basePath = "auto-announce.messages.";
                break;
            case "join-message":
                basePath = "join-features.join-messages.messages.";
                break;
            case "first-join-message":
                basePath = "join-features.first-join-messages.messages.";
                break;
            default:

                player.sendMessage(prefix + ChatColor.RED + "Error: Unknown data type.");
                return;
        }

        if (!isCreator && originalName != null && !originalName.equals(name)) {
            plugin.getConfig().set(basePath + originalName, null);
        }

        if (plugin.getConfig().contains(basePath + name) && (isCreator || !originalName.equals(name))) {

            player.sendMessage(prefix + ChatColor.RED + "An item with this name already exists!");
            return;
        }

        ChatInputListener.activeSessions.remove(player.getUniqueId());

        plugin.getConfig().set(basePath + name + ".message", data.get("message"));
        plugin.getConfig().set(basePath + name + ".style", data.get("style"));
        plugin.getConfig().set(basePath + name + ".icon", data.get("icon"));
        plugin.getConfig().set(basePath + name + ".custom-model-data", data.get("custom-model-data"));

        plugin.saveConfig();
        if (type.equals("auto-announce")) {
            AutoAnnounce.stopAutoAnnounce();
            AutoAnnounce.startAutoAnnounce();
        }

        player.sendMessage(prefix + ChatColor.GREEN + "Changes saved successfully!");

        returnToPreviousMenu(player, type);
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