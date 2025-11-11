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

public class PresetsGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    private static final int SLOT_BACK_BUTTON = 45;
    private static final int SLOT_ADD_ITEM = 53;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title")));
        ConfigurationSection presetsSection = plugin.getConfig().getConfigurationSection("presets");
        if (presetsSection != null) {
            for (String key : presetsSection.getKeys(false)) {
                ConfigurationSection preset = presetsSection.getConfigurationSection(key);
                if (preset == null) continue;

                String iconStr = preset.getString("icon", "PAPER");
                Material iconMaterial;
                try {
                    iconMaterial = Material.valueOf(iconStr.toUpperCase());
                } catch (Exception e) {
                    iconMaterial = Material.PAPER;
                }

                ItemStack item = new ItemStack(iconMaterial);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + key);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.translateAlternateColorCodes('&', "&f&nCurrent Message:"));
                lore.add(" ");
                addFormattedMessage(lore, preset.getString("message"));
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Style: " + ChatColor.WHITE + preset.getString("style", "GOAL"));
                String cmd = preset.getString("custom-model-data", "");
                lore.add(ChatColor.GRAY + "CustomModelData: " + ChatColor.WHITE + (cmd.isEmpty() ? "None" : cmd));
                lore.add(" ");
                lore.add(ChatColor.YELLOW + "Left click to edit this preset.");
                lore.add(ChatColor.RED + "Right click to delete this preset.");
                meta.setLore(lore);
                item.setItemMeta(meta);
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
        addMeta.setDisplayName(ChatColor.GREEN + "Add Preset");
        addItem.setItemMeta(addMeta);
        gui.setItem(SLOT_ADD_ITEM, addItem);

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getSlot();

        if (slot == SLOT_BACK_BUTTON) {
            MainMenuGUI.open(player);
            return;
        }

        if (slot == SLOT_ADD_ITEM) {
            Map<String, Object> data = new HashMap<>();
            data.put("isCreator", true);
            data.put("type", "preset");
            data.put("name", "<not set>");
            data.put("message", "Default message");
            data.put("style", "GOAL");
            data.put("icon", "GRASS_BLOCK");
            data.put("custom-model-data", "");
            EditorGUI.open(player, data);
            return;
        }

        if (clickedItem != null && clickedItem.getType() != Material.AIR && slot < 45) {
            if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

            String presetName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (!plugin.getConfig().contains("presets." + presetName)) return;

            if (event.isRightClick()) {
                ConfirmationGUI.open(player, presetName);
                GUIHandler.confirmationActions.put(player.getUniqueId(), () -> {
                    plugin.getConfig().set("presets." + presetName, null);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Preset '" + presetName + "' has been deleted.");
                    open(player);
                });
            } else if (event.isLeftClick()) {
                ConfigurationSection preset = plugin.getConfig().getConfigurationSection("presets." + presetName);
                Map<String, Object> data = new HashMap<>();
                data.put("isCreator", false);
                data.put("originalName", presetName);
                data.put("type", "preset");
                data.put("name", presetName);
                data.put("message", preset.getString("message"));
                data.put("style", preset.getString("style", "GOAL"));
                data.put("icon", preset.getString("icon", "STONE"));
                data.put("custom-model-data", preset.getString("custom-model-data", ""));
                EditorGUI.open(player, data);
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