package io.github.altkat.advancementannouncer.guis;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.Handlers.GUIHandler;
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
        String title = type.equals("join") ? ChatColor.BLUE + "Normal Join Messages" : ChatColor.GOLD + "First Join Messages";

        Inventory gui = Bukkit.createInventory(null, 54, title);
        ConfigurationSection mainSection = plugin.getConfig().getConfigurationSection(configPath);

        if (mainSection == null) {
            plugin.getConfig().createSection(configPath);
            mainSection = plugin.getConfig().getConfigurationSection(configPath);
        }

        boolean isEnabled = mainSection.getBoolean("enabled");
        ItemStack toggleItem = new ItemStack(isEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleItem.getItemMeta();
        toggleMeta.setDisplayName(ChatColor.YELLOW + "Status: " + (isEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        toggleItem.setItemMeta(toggleMeta);
        gui.setItem(SLOT_TOGGLE, toggleItem);

        ConfigurationSection messagesSection = mainSection.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                String iconStr = messagesSection.getString(key + ".icon", "PAPER");
                String cmdStr = messagesSection.getString(key + ".custom-model-data", "");
                String displayName = ChatColor.GREEN + key;

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.WHITE + "Message: ");
                addFormattedMessage(lore, messagesSection.getString(key + ".message"));
                lore.add(" ");
                lore.add(ChatColor.WHITE + "Style: " + messagesSection.getString(key + ".style"));
                lore.add(ChatColor.WHITE + "Icon: " + iconStr);
                lore.add(ChatColor.WHITE + "CustomModelData: " + (cmdStr.isEmpty() ? "None" : cmdStr));
                lore.add(" ");
                lore.add(ChatColor.YELLOW + "Left click to edit.");
                lore.add(ChatColor.RED + "Right click to delete.");

                ItemStack item = GUIHandler.createDisplayItem(iconStr, cmdStr, displayName, lore);
                gui.addItem(item);
            }
        }

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backItem.setItemMeta(backMeta);
        gui.setItem(SLOT_BACK_BUTTON, backItem);

        ItemStack addItem = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Add Message");
        addItem.setItemMeta(addMeta);
        gui.setItem(SLOT_ADD_ITEM, addItem);

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
                    player.sendMessage(ChatColor.GREEN + "Message '" + messageName + "' has been deleted.");
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
                EditorGUI.open(player, editData);
            }
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