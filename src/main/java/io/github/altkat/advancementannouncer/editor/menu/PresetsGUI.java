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

public class PresetsGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    private static final int SLOT_BACK_BUTTON = 45;
    private static final int SLOT_ADD_ITEM = 53;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TextUtil.color("&#7688FFAdvancement Announcer Presets"));
        ConfigurationSection presetsSection = plugin.getConfig().getConfigurationSection("presets");
        if (presetsSection != null) {
            for (String key : presetsSection.getKeys(false)) {
                ConfigurationSection preset = presetsSection.getConfigurationSection(key);
                if (preset == null) continue;

                String iconStr = preset.getString("icon", "PAPER");
                String cmdStr = preset.getString("custom-model-data", "");
                String styleStr = preset.getString("style", "GOAL");
                String soundStr = preset.getString("sound", "");
                String displayName = TextUtil.color("&#76FF90" + key);

                List<String> lore = new ArrayList<>();
                lore.add(TextUtil.color("&fCurrent Message:"));
                lore.add(" ");
                addFormattedMessage(lore, preset.getString("message"));
                lore.add(" ");
                lore.add(TextUtil.color("&#FCD05C» &#76FF90Style: &f" + styleStr));
                lore.add(TextUtil.color("&#FCD05C» &#76FF90Icon: &f" + iconStr));
                lore.add(TextUtil.color("&#FCD05C» &#76FF90CustomModelData: &f" + (cmdStr.isEmpty() ? "None" : cmdStr)));
                lore.add(TextUtil.color("&#FCD05C» &#76FF90Sound: &f" + (soundStr.isEmpty() ? "None" : soundStr)));

                lore.add(" ");
                lore.add(TextUtil.color("&#FCD05CLeft-click to edit this preset."));
                lore.add(TextUtil.color("&#F86B6BRight-click to delete this preset."));

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
        addMeta.setDisplayName(TextUtil.color("&#76FF90Add Preset"));
        addItem.setItemMeta(addMeta);
        gui.setItem(SLOT_ADD_ITEM, addItem);

        GUIHandler.fillBackground(gui);

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
                    player.sendMessage(TextUtil.color("&#76FF90Preset '" + presetName + "&#76FF90' has been deleted."));
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
                data.put("sound", preset.getString("sound", ""));
                EditorGUI.open(player, data);
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