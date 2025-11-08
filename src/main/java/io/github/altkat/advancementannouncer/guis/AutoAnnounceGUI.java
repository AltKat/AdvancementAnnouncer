package io.github.altkat.advancementannouncer.guis;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.Handlers.AutoAnnounce;
import io.github.altkat.advancementannouncer.Handlers.ChatInputListener;
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

public class AutoAnnounceGUI {
    private static final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    private static final int SLOT_AA_TOGGLE = 48;
    private static final int SLOT_AA_INTERVAL = 49;
    private static final int SLOT_AA_MODE = 50;
    private static final int SLOT_ADD_ITEM = 53;
    private static final int SLOT_BACK_BUTTON = 45;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.auto-announce-gui-title")));
        ConfigurationSection aaSection = plugin.getConfig().getConfigurationSection("auto-announce");

        ItemStack enabledItem = new ItemStack(aaSection.getBoolean("enabled") ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta enabledMeta = enabledItem.getItemMeta();
        enabledMeta.setDisplayName(ChatColor.YELLOW + "Auto Announce: " + (aaSection.getBoolean("enabled") ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        enabledItem.setItemMeta(enabledMeta);
        gui.setItem(SLOT_AA_TOGGLE, enabledItem);

        ItemStack intervalItem = new ItemStack(Material.CLOCK);
        ItemMeta intervalMeta = intervalItem.getItemMeta();
        intervalMeta.setDisplayName(ChatColor.YELLOW + "Interval: " + ChatColor.GOLD + aaSection.getInt("interval") + "s");
        intervalItem.setItemMeta(intervalMeta);
        gui.setItem(SLOT_AA_INTERVAL, intervalItem);

        ItemStack modeItem = new ItemStack(Material.COMPARATOR);
        ItemMeta modeMeta = modeItem.getItemMeta();
        modeMeta.setDisplayName(ChatColor.YELLOW + "Mode: " + ChatColor.GOLD + aaSection.getString("mode"));
        modeItem.setItemMeta(modeMeta);
        gui.setItem(SLOT_AA_MODE, modeItem);

        ConfigurationSection messagesSection = aaSection.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                Material iconMaterial;
                try {
                    iconMaterial = Material.valueOf(messagesSection.getString(key + ".icon").toUpperCase());
                } catch (IllegalArgumentException | NullPointerException e) {
                    iconMaterial = Material.PAPER;
                }

                ItemStack item = new ItemStack(iconMaterial);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + key);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.WHITE + "Message: ");
                addFormattedMessage(lore, messagesSection.getString(key + ".message"));
                lore.add(" ");
                lore.add(ChatColor.WHITE + "Style: " + messagesSection.getString(key + ".style"));
                lore.add(ChatColor.WHITE + "Icon: " + messagesSection.getString(key + ".icon"));
                lore.add(" ");
                lore.add(ChatColor.YELLOW + "Left click to edit.");
                lore.add(ChatColor.RED + "Right click to delete.");
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
        addMeta.setDisplayName(ChatColor.GREEN + "Add Message");
        addItem.setItemMeta(addMeta);
        gui.setItem(SLOT_ADD_ITEM, addItem);

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
                player.sendMessage(ChatColor.YELLOW + "Please type the new interval (in seconds) in chat.");
                player.sendMessage(ChatColor.GRAY + "Current: " + plugin.getConfig().getInt("auto-announce.interval"));
                player.sendMessage(ChatColor.GRAY + "(Type 'cancel' to exit)");
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
                            player.sendMessage(ChatColor.GREEN + "Auto-announce message '" + messageName + "' has been deleted.");
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
                        EditorGUI.open(player, editData);
                    }
                }
                break;
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