package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.editor.GUIHandler;
import io.github.altkat.advancementannouncer.feature.AutoAnnounce;
import io.github.altkat.advancementannouncer.editor.ChatInputListener;
import io.github.altkat.advancementannouncer.util.TextUtil;
import org.bukkit.Bukkit;
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
    private static final int SLOT_SET_NAME = 11;
    private static final int SLOT_SET_MESSAGE = 13;
    private static final int SLOT_SET_STYLE = 15;
    private static final int SLOT_SET_ICON = 29;
    private static final int SLOT_SET_CUSTOM_MODEL_DATA = 31;
    private static final int SLOT_SET_SOUND = 33;
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
        data.putIfAbsent("sound", "");

        ChatInputListener.activeSessions.put(player.getUniqueId(), data);

        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nameItem.getItemMeta();
        nameMeta.setDisplayName(TextUtil.color("&#FCD05CSet Name"));
        List<String> nameLore = new ArrayList<>();
        nameLore.add(TextUtil.color("&7Current: " + data.get("name")));
        nameLore.add(" ");
        nameLore.add(TextUtil.color("&#76FF90Click to change the name via chat."));
        nameMeta.setLore(nameLore);
        nameItem.setItemMeta(nameMeta);
        gui.setItem(SLOT_SET_NAME, nameItem);

        ItemStack messageItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta messageMeta = messageItem.getItemMeta();
        messageMeta.setDisplayName(TextUtil.color("&bSet Message"));
        List<String> messageLore = new ArrayList<>();
        messageLore.add(TextUtil.color("&7Current: "));
        addFormattedMessage(messageLore, (String) data.get("message"));
        messageLore.add(" ");
        messageLore.add(TextUtil.color("&#76FF90Click to change the message via chat."));
        messageMeta.setLore(messageLore);
        messageItem.setItemMeta(messageMeta);
        gui.setItem(SLOT_SET_MESSAGE, messageItem);

        ItemStack styleItem = new ItemStack(Material.PAINTING);
        ItemMeta styleMeta = styleItem.getItemMeta();
        styleMeta.setDisplayName(TextUtil.color("&6Set Style"));
        List<String> styleLore = new ArrayList<>();
        styleLore.add(TextUtil.color("&7Current: " + data.get("style")));
        styleLore.add(" ");
        styleLore.add(TextUtil.color("&#76FF90Click to choose a style."));
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
        iconMeta.setDisplayName(TextUtil.color("&dSet Icon"));
        List<String> iconLore = new ArrayList<>();
        iconLore.add(TextUtil.color("&7Current: " + data.get("icon")));
        iconLore.add(" ");
        iconLore.add(TextUtil.color("&#76FF90Click to choose an icon."));
        iconMeta.setLore(iconLore);
        iconItem.setItemMeta(iconMeta);
        gui.setItem(SLOT_SET_ICON, iconItem);

        ItemStack iconCmdItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta iconCmdMeta = iconCmdItem.getItemMeta();
        iconCmdMeta.setDisplayName(TextUtil.color("&3Set CustomModelData"));
        List<String> iconCmdLore = new ArrayList<>();
        String currentCmd = data.get("custom-model-data").toString();
        iconCmdLore.add(TextUtil.color("&7Current: " + (currentCmd.isEmpty() ? "None" : currentCmd)));
        iconCmdLore.add(" ");
        iconCmdLore.add(TextUtil.color("&#76FF90Click to change via chat."));
        iconCmdLore.add(TextUtil.color("&7(e.g., '12345', 'itemsadder:my_item',"));
        iconCmdLore.add(TextUtil.color("&7'nexo:my_item', or 'none' to clear.)"));
        iconCmdMeta.setLore(iconCmdLore);
        iconCmdItem.setItemMeta(iconCmdMeta);
        gui.setItem(SLOT_SET_CUSTOM_MODEL_DATA, iconCmdItem);

        ItemStack soundItem = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta soundMeta = soundItem.getItemMeta();
        soundMeta.setDisplayName(TextUtil.color("&bSet Sound"));
        List<String> soundLore = new ArrayList<>();
        String currentSound = data.get("sound").toString();
        soundLore.add(TextUtil.color("&7Current: " + (currentSound.isEmpty() ? "None" : currentSound)));
        soundLore.add(" ");
        soundLore.add(TextUtil.color("&#76FF90Click to select a sound."));
        soundMeta.setLore(soundLore);
        soundItem.setItemMeta(soundMeta);
        gui.setItem(SLOT_SET_SOUND, soundItem);

        ItemStack saveItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName(TextUtil.color("&#76FF90Save"));
        saveItem.setItemMeta(saveMeta);
        gui.setItem(SLOT_SAVE, saveItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(TextUtil.color("&#F86B6BCancel"));
        backItem.setItemMeta(backMeta);
        gui.setItem(SLOT_CANCEL, backItem);

        GUIHandler.fillBackground(gui);

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
                player.sendMessage(TextUtil.color("&#76FF90Please type the new name in chat. (Type 'cancel' to abort)"));
                player.sendMessage(TextUtil.color("&7Current value: " + data.get("name")));
                data.put("step", ChatInputListener.STEP_NAME);
                break;
            case SLOT_SET_MESSAGE:
                player.closeInventory();
                player.sendMessage(TextUtil.color("&#76FF90Please type the new message in chat. (Use | for new line, type 'cancel' to abort)"));
                player.sendMessage(TextUtil.color("&7Current value: " + data.get("message")));
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
                player.sendMessage(TextUtil.color("&#76FF90Please type the new CustomModelData value."));
                player.sendMessage(TextUtil.color("&7(e.g., '12345', 'itemsadder:my_item', 'nexo:my_item', or 'none' to clear)"));
                player.sendMessage(TextUtil.color("&7Current value: " + data.get("custom-model-data")));
                data.put("step", ChatInputListener.STEP_CUSTOM_MODEL_DATA);
                break;
            case SLOT_SET_SOUND:
                SoundSelectionGUI.open(player);
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
            player.sendMessage(prefix + TextUtil.color("&#F86B6BYou must set a name before saving!"));
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

                player.sendMessage(prefix + TextUtil.color("&#F86B6BError: Unknown data type."));
                return;
        }

        if (!isCreator && originalName != null && !originalName.equals(name)) {
            plugin.getConfig().set(basePath + originalName, null);
        }

        if (plugin.getConfig().contains(basePath + name) && (isCreator || !originalName.equals(name))) {

            player.sendMessage(prefix + TextUtil.color("&#F86B6BAn item with this name already exists!"));
            return;
        }

        ChatInputListener.activeSessions.remove(player.getUniqueId());

        plugin.getConfig().set(basePath + name + ".message", data.get("message"));
        plugin.getConfig().set(basePath + name + ".style", data.get("style"));
        plugin.getConfig().set(basePath + name + ".icon", data.get("icon"));
        plugin.getConfig().set(basePath + name + ".custom-model-data", data.get("custom-model-data"));
        plugin.getConfig().set(basePath + name + ".sound", data.get("sound"));

        plugin.saveConfig();
        if (type.equals("auto-announce")) {
            AutoAnnounce.stopAutoAnnounce();
            AutoAnnounce.startAutoAnnounce();
        }

        player.sendMessage(prefix + TextUtil.color("&#76FF90Changes saved successfully!"));

        returnToPreviousMenu(player, type);
    }

    private static void addFormattedMessage(List<String> lore, String message) {
        if (message != null && message.contains("|")) {
            for (String line : message.split("\\|")) {
                lore.add(TextUtil.color(line));
            }
        } else if (message != null) {
            lore.add(TextUtil.color(message));
        }
    }
}