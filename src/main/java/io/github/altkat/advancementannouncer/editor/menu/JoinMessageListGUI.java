package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.editor.GUIHandler;
import io.github.altkat.advancementannouncer.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinMessageListGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    private static final int SLOT_TOGGLE = 49;
    private static final int SLOT_ADD_ITEM = 53;
    private static final int SLOT_BACK_BUTTON = 45;

    public static void open(Player player, String type) {
        String configPath = type.equals("join") ? "join-features.join-messages" : "join-features.first-join-messages";
        String title = TextUtil.color(type.equals("join") ? "&#7688FFNormal Join Messages" : "&#7688FFFirst Join Messages");

        Inventory gui = Bukkit.createInventory(null, 54, title);
        ConfigurationSection mainSection = plugin.getConfig().getConfigurationSection(configPath);

        if (mainSection == null) {
            plugin.getConfig().createSection(configPath);
            mainSection = plugin.getConfig().getConfigurationSection(configPath);
        }

        boolean isEnabled = mainSection.getBoolean("enabled");
        ItemStack toggleItem = new ItemStack(isEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleItem.getItemMeta();
        toggleMeta.setDisplayName(TextUtil.color("&#FCD05CStatus: " + (isEnabled ? "&#76FF90Enabled" : "&#F86B6BDisabled")));
        toggleItem.setItemMeta(toggleMeta);
        gui.setItem(SLOT_TOGGLE, toggleItem);

        ConfigurationSection messagesSection = mainSection.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                String iconStr = messagesSection.getString(key + ".icon", "PAPER");
                String cmdStr = messagesSection.getString(key + ".custom-model-data", "");
                String styleStr = messagesSection.getString(key + ".style");
                String soundStr = messagesSection.getString(key + ".sound", "");
                String displayName = TextUtil.color("&#76FF90" + key);

                List<String> lore = new ArrayList<>();
                lore.add(TextUtil.color("&fMessage: "));
                addFormattedMessage(lore, messagesSection.getString(key + ".message"));
                lore.add(" ");
                lore.add(TextUtil.color("&#FCD05C» &#76FF90Style: &f" + styleStr));
                lore.add(TextUtil.color("&#FCD05C» &#76FF90Icon: &f" + iconStr));
                lore.add(TextUtil.color("&#FCD05C» &#76FF90CustomModelData: &f" + (cmdStr.isEmpty() ? "None" : cmdStr)));
                lore.add(TextUtil.color("&#FCD05C» &#76FF90Sound: &f" + (soundStr.isEmpty() ? "None" : soundStr)));
                lore.add(" ");
                lore.add(TextUtil.color("&#FCD05CLeft-click to edit."));
                lore.add(TextUtil.color("&#F86B6BRight-click to delete."));

                ItemStack item = GUIHandler.createDisplayItem(iconStr, cmdStr, displayName, lore);
                gui.addItem(item);
            }
        }

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(TextUtil.color("&#F86B6BBack"));
        backItem.setItemMeta(backMeta);
        gui.setItem(SLOT_BACK_BUTTON, backItem);

        ItemStack addItem = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.setDisplayName(TextUtil.color("&#76FF90Add Message"));
        addItem.setItemMeta(addMeta);
        gui.setItem(SLOT_ADD_ITEM, addItem);

        GUIHandler.fillBackground(gui);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        String type = title.contains("First") ? "first-join" : "join";
        String configPath = type.equals("join") ? "join-features.join-messages" : "join-features.first-join-messages";

        int slot = event.getSlot();

        if (slot == SLOT_BACK_BUTTON) {
            JoinFeaturesGUI.open(player);
            return;
        }

        if (slot == SLOT_TOGGLE) {
            boolean currentStatus = plugin.getConfig().getBoolean(configPath + ".enabled");
            plugin.getConfig().set(configPath + ".enabled", !currentStatus);
            plugin.saveConfig();
            open(player, type);
            return;
        }

        if (slot == SLOT_ADD_ITEM) {
            Map<String, Object> data = new HashMap<>();
            data.put("isCreator", true);
            data.put("type", type + "-message");
            data.put("name", "<not set>");
            data.put("message", "Welcome!");
            data.put("style", "GOAL");
            data.put("icon", "GRASS_BLOCK");
            data.put("custom-model-data", "");
            data.put("sound", "");
            EditorGUI.open(player, data);
            return;
        }

        if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null && slot < 45) {
            String messageName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            if (event.isRightClick()) {
                ConfirmationGUI.open(player, messageName);
                GUIHandler.confirmationActions.put(player.getUniqueId(), () -> {
                    plugin.getConfig().set(configPath + ".messages." + messageName, null);
                    plugin.saveConfig();
                    player.sendMessage(TextUtil.color("&#76FF90Message '" + messageName + "&#76FF90' has been deleted."));
                    open(player, type);
                });
            } else if (event.isLeftClick()) {
                Map<String, Object> editData = new HashMap<>();
                String path = configPath + ".messages." + messageName;
                editData.put("isCreator", false);
                editData.put("originalName", messageName);
                editData.put("type", type + "-message");
                editData.put("name", messageName);
                editData.put("message", plugin.getConfig().getString(path + ".message", ""));
                editData.put("style", plugin.getConfig().getString(path + ".style", "GOAL"));
                editData.put("icon", plugin.getConfig().getString(path + ".icon", "STONE"));
                editData.put("custom-model-data", plugin.getConfig().getString(path + ".custom-model-data", ""));
                editData.put("sound", plugin.getConfig().getString(path + ".sound", ""));
                EditorGUI.open(player, editData);
            }
        }
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