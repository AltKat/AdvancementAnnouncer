package io.github.altkat.advancementannouncer.editor.menu;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.feature.AutoAnnounce;
import io.github.altkat.advancementannouncer.editor.ChatInputListener;
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

public class AutoAnnounceGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    private static final int SLOT_AA_TOGGLE = 48;
    private static final int SLOT_AA_INTERVAL = 49;
    private static final int SLOT_AA_MODE = 50;
    private static final int SLOT_ADD_ITEM = 53;
    private static final int SLOT_BACK_BUTTON = 45;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TextUtil.color("&#7688FFAuto Announce Config"));
        ConfigurationSection aaSection = plugin.getConfig().getConfigurationSection("auto-announce");

        ItemStack enabledItem = new ItemStack(aaSection.getBoolean("enabled") ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta enabledMeta = enabledItem.getItemMeta();
        enabledMeta.setDisplayName(TextUtil.color("&#FCD05CAuto Announce: " + (aaSection.getBoolean("enabled") ? "&#76FF90Enabled" : "&#F86B6BDisabled")));
        enabledItem.setItemMeta(enabledMeta);
        gui.setItem(SLOT_AA_TOGGLE, enabledItem);

        ItemStack intervalItem = new ItemStack(Material.CLOCK);
        ItemMeta intervalMeta = intervalItem.getItemMeta();
        intervalMeta.setDisplayName(TextUtil.color("&#FCD05CInterval: &6" + aaSection.getInt("interval") + "s"));
        intervalItem.setItemMeta(intervalMeta);
        gui.setItem(SLOT_AA_INTERVAL, intervalItem);

        ItemStack modeItem = new ItemStack(Material.COMPARATOR);
        ItemMeta modeMeta = modeItem.getItemMeta();
        modeMeta.setDisplayName(TextUtil.color("&#FCD05CMode: &6" + aaSection.getString("mode")));
        modeItem.setItemMeta(modeMeta);
        gui.setItem(SLOT_AA_MODE, modeItem);

        ConfigurationSection messagesSection = aaSection.getConfigurationSection("messages");
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
        int slot = event.getSlot();

        if (slot == SLOT_BACK_BUTTON) {
            MainMenuGUI.open(player);
            return;
        }

        switch (slot) {
            case SLOT_AA_TOGGLE:
                boolean currentStatus = plugin.getConfig().getBoolean("auto-announce.enabled");
                plugin.getConfig().set("auto-announce.enabled", !currentStatus);
                plugin.saveConfig();
                AutoAnnounce.stopAutoAnnounce();
                AutoAnnounce.startAutoAnnounce();
                open(player);
                break;
            case SLOT_AA_INTERVAL:
                player.closeInventory();
                player.sendMessage(TextUtil.color("&#FCD05CPlease type the new interval (in seconds) in chat."));
                player.sendMessage(TextUtil.color("&7Current: " + plugin.getConfig().getInt("auto-announce.interval")));
                player.sendMessage(TextUtil.color("&7(Type 'cancel' to exit)"));
                ChatInputListener.activeSessions.put(player.getUniqueId(), new HashMap<>() {{
                    put("step", ChatInputListener.STEP_INTERVAL);
                }});
                break;
            case SLOT_AA_MODE:
                String currentMode = plugin.getConfig().getString("auto-announce.mode");
                String newMode = currentMode.equalsIgnoreCase("ORDERED") ? "RANDOM" : "ORDERED";
                plugin.getConfig().set("auto-announce.mode", newMode);
                plugin.saveConfig();
                AutoAnnounce.stopAutoAnnounce();
                AutoAnnounce.startAutoAnnounce();
                open(player);
                break;
            case SLOT_ADD_ITEM:
                Map<String, Object> data = new HashMap<>();
                data.put("isCreator", true);
                data.put("type", "auto-announce");
                data.put("name", "<not set>");
                data.put("message", "Default message");
                data.put("style", "GOAL");
                data.put("icon", "STONE");
                data.put("custom-model-data", "");
                data.put("sound", "");
                EditorGUI.open(player, data);
                break;
            default:
                if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null && slot < SLOT_BACK_BUTTON) {
                    String messageName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (event.isRightClick()) {
                        ConfirmationGUI.open(player, messageName);
                        GUIHandler.confirmationActions.put(player.getUniqueId(), () -> {
                            plugin.getConfig().set("auto-announce.messages." + messageName, null);
                            plugin.saveConfig();
                            AutoAnnounce.stopAutoAnnounce();
                            AutoAnnounce.startAutoAnnounce();
                            player.sendMessage(TextUtil.color("&#76FF90Auto-announce message '" + messageName + "&#76FF90' has been deleted."));
                            open(player);
                        });
                    } else if (event.isLeftClick()) {
                        Map<String, Object> editData = new HashMap<>();
                        String path = "auto-announce.messages." + messageName;
                        editData.put("isCreator", false);
                        editData.put("originalName", messageName);
                        editData.put("type", "auto-announce");
                        editData.put("name", messageName);
                        editData.put("message", plugin.getConfig().getString(path + ".message", ""));
                        editData.put("style", plugin.getConfig().getString(path + ".style", "GOAL"));
                        editData.put("icon", plugin.getConfig().getString(path + ".icon", "STONE"));
                        editData.put("custom-model-data", plugin.getConfig().getString(path + ".custom-model-data", ""));
                        editData.put("sound", plugin.getConfig().getString(path + ".sound", ""));
                        EditorGUI.open(player, editData);
                    }
                }
                break;
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