package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter, Listener {
    AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();

    private static final int SLOT_EDIT_AUTO_ANNOUNCE = 11;
    private static final int SLOT_EDIT_PRESETS = 15;
    private static final int SLOT_BACK_BUTTON = 45;
    private static final int SLOT_AA_TOGGLE = 48;
    private static final int SLOT_AA_INTERVAL = 49;
    private static final int SLOT_AA_MODE = 50;
    private static final int SLOT_ADD_ITEM = 53;

    private static final String STEP_NAME = "name";
    private static final String STEP_MESSAGE = "message";
    private static final String STEP_STYLE = "style";
    private static final String STEP_ICON = "icon";
    private static final String STEP_INTERVAL = "interval";

    private final Map<UUID, String> presetCreators = new HashMap<>();
    private final Map<UUID, String> presetEditors = new HashMap<>();
    private final Map<UUID, Map<String, String>> autoAnnounceCreators = new HashMap<>();
    private final Map<UUID, Map<String, String>> autoAnnounceEditors = new HashMap<>();

    private final Set<UUID> isNavigating = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("edit")) {
                if (sender instanceof Player) {
                    if (sender.hasPermission("advancementannouncer.admin")) {
                        openEditGUI((Player) sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                }
                return true;
            }
        }


        if(!sender.hasPermission("advancementannouncer.admin")){
            if(args.length != 1 || !args[0].equalsIgnoreCase("toggle")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.wrong-usage")));
                return true;
            }else{
                UUID playerUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
                PlayerData.setToggleData(playerUUID, !PlayerData.returnToggleData(playerUUID));
                if(PlayerData.returnToggleData(playerUUID)){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-on")));
                }else{
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-off")));
                }
            }
            return true;
        }

        if(args.length == 0){
            sendHelpMessage(sender);
            return true;
        }

        if(args[0].equalsIgnoreCase("toggle")){
            if(!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                return true;
            }
            UUID playerUUID = Bukkit.getPlayer(sender.getName()).getUniqueId();
            PlayerData.setToggleData(playerUUID, !PlayerData.returnToggleData(playerUUID));
            if(PlayerData.returnToggleData(playerUUID)){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-on")));
            }else{
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.announcements-toggled-off")));
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("reload")){
            AutoAnnounce.stopAutoAnnounce();
            plugin.reloadConfig();
            AutoAnnounce.startAutoAnnounce();
            PlayerData.reloadPlayerData();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.config-reloaded")));
            return true;
        }

        if (args.length < 4){
            sendHelpMessage(sender);
            return true;
        }

        final AdvancementHandler.Style style;

        try {
            style = AdvancementHandler.Style.valueOf(args[0].toUpperCase());

        } catch (final Throwable t) {
            sender.sendMessage(ChatColor.RED + "Invalid style: " + args[0]);

            return true;
        }

        final String materialName = args[1];

        try {
            Material.valueOf(materialName.toUpperCase());

        } catch (final Throwable t) {
            sender.sendMessage(ChatColor.RED + "Invalid material: " + materialName);

            return true;
        }

        String audience = args[2];

        String message = "";

        if(plugin.getConfig().getConfigurationSection("presets").getKeys(false).contains(args[3])) {
            message = plugin.getConfig().getString("presets." + args[3]);
        }else {

            for (int i = 3; i < args.length; i++)
                message += args[i] + " ";

        }
        message = ChatColor.translateAlternateColorCodes('&', message.trim());

        if(audience.equalsIgnoreCase("all")){
            if(sender.getServer().getOnlinePlayers().isEmpty()){
                sender.sendMessage(ChatColor.RED + "There are no online players in the server!");
                return true;
            }
            for(Player player : sender.getServer().getOnlinePlayers()){
                if(!PlayerData.returnToggleData(player.getUniqueId())){
                    continue;
                }
                AdvancementHandler.displayTo(player, materialName, message, style);
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to all players"));
        }else{
            Player player = sender.getServer().getPlayer(audience);
            if(player == null){
                sender.sendMessage(ChatColor.RED + "Player not found: " + audience);
                return true;
            }
            AdvancementHandler.displayTo(player, materialName, message, style);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to " + player.getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final List<String> tab = new ArrayList<>();
        if(!sender.hasPermission("advancementannouncer.admin")){
            if(args.length == 1){
                tab.add("toggle");
                return tab;
            }
            return tab;
        }

        switch (args.length) {
            case 1:
                tab.add("reload");
                tab.add("toggle");
                tab.add("edit");
                for (final AdvancementHandler.Style style : AdvancementHandler.Style.values())
                    tab.add(style.toString().toLowerCase());
                break;

            case 2:
                if(args[0].equalsIgnoreCase("goal") || args[0].equalsIgnoreCase("challenge") || args[0].equalsIgnoreCase("task")) {
                    for (final Material material : Material.values())
                        tab.add(material.toString().toLowerCase());
                }
                break;

            case 3:
                tab.add("all");
                for (final Player player : sender.getServer().getOnlinePlayers()) {
                    tab.add(player.getName());
                }
                break;
            case 4:
                tab.addAll(plugin.getConfig().getConfigurationSection("presets").getKeys(false));
                tab.add("your message");
                break;
        }

        return tab.stream().filter(completion -> completion.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

    private void sendHelpMessage(CommandSender sender){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aCommands: "));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa <style> <material> <player> <message>"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa reload"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa toggle"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/aa edit"));
    }

    private void openEditGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.edit-gui-title")));

        ItemStack autoAnnounceItem = new ItemStack(Material.CLOCK);
        ItemMeta autoAnnounceMeta = autoAnnounceItem.getItemMeta();
        autoAnnounceMeta.setDisplayName(ChatColor.AQUA + "Edit Auto Announce");
        autoAnnounceItem.setItemMeta(autoAnnounceMeta);
        gui.setItem(SLOT_EDIT_AUTO_ANNOUNCE, autoAnnounceItem);

        ItemStack presetsItem = new ItemStack(Material.PAPER);
        ItemMeta presetsMeta = presetsItem.getItemMeta();
        presetsMeta.setDisplayName(ChatColor.GREEN + "Edit Presets");
        presetsItem.setItemMeta(presetsMeta);
        gui.setItem(SLOT_EDIT_PRESETS, presetsItem);

        player.openInventory(gui);
    }

    private void openPresetsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title")));
        ConfigurationSection presetsSection = plugin.getConfig().getConfigurationSection("presets");
        if (presetsSection != null) {
            for (String key : presetsSection.getKeys(false)) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + key);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.translateAlternateColorCodes('&', "&f&nCurrent Message:"));
                lore.add(" ");
                String presetMessage = presetsSection.getString(key);
                if (presetMessage.contains("|")) {
                    for (String line : presetMessage.split("\\|")) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                } else {
                    lore.add(ChatColor.translateAlternateColorCodes('&', presetMessage));
                }
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

    private void openAutoAnnounceGUI(Player player) {
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
                    plugin.getLogger().warning("Invalid icon material for auto-announce message '" + key + "'. Using PAPER as a fallback.");
                }

                ItemStack item = new ItemStack(iconMaterial);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + key);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.WHITE + "Message: " + ChatColor.translateAlternateColorCodes('&', messagesSection.getString(key + ".message")));
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String editGUITitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.edit-gui-title"));
        String presetsGUITitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title"));
        String autoAnnounceGUITitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.auto-announce-gui-title"));
        String clickedGUITitle = event.getView().getTitle();

        if (!clickedGUITitle.equals(editGUITitle) && !clickedGUITitle.equals(presetsGUITitle) && !clickedGUITitle.equals(autoAnnounceGUITitle)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }
        isNavigating.add(player.getUniqueId());

        if (clickedGUITitle.equals(editGUITitle)) {
            if (event.getSlot() == SLOT_EDIT_AUTO_ANNOUNCE) {
                openAutoAnnounceGUI(player);
            } else if (event.getSlot() == SLOT_EDIT_PRESETS) {
                openPresetsGUI(player);
            }
        } else if (clickedGUITitle.equals(presetsGUITitle)) {
            handlePresetsGUIClick(event);
        } else if (clickedGUITitle.equals(autoAnnounceGUITitle)) {
            handleAutoAnnounceGUIClick(event);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> isNavigating.remove(player.getUniqueId()), 1L);
    }

    private void handlePresetsGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getSlot() == SLOT_BACK_BUTTON) {
            openEditGUI(player);
            return;
        }

        if (event.getSlot() == SLOT_ADD_ITEM) {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Please type the name of the new preset in chat. " + ChatColor.GRAY + "(Type 'cancel' to exit)");
            presetCreators.put(player.getUniqueId(), STEP_NAME);
        } else if (clickedItem.getType() == Material.PAPER) {
            String presetName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            if (event.isRightClick()) {
                plugin.getConfig().set("presets." + presetName, null);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Preset '" + presetName + "' has been deleted.");
                openPresetsGUI(player);
            } else if (event.isLeftClick()){
                player.closeInventory();
                String currentMessage = plugin.getConfig().getString("presets." + presetName);
                player.sendMessage(ChatColor.YELLOW + "Please type the new message for the preset '" + presetName + "'. (Use | for a new line)");
                player.sendMessage(ChatColor.GRAY + "Current: " + currentMessage);
                player.sendMessage(ChatColor.GRAY + "(Type 'cancel' to exit)");
                presetEditors.put(player.getUniqueId(), presetName);
            }
        }
    }

    private void handleAutoAnnounceGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == SLOT_BACK_BUTTON) {
            openEditGUI(player);
            return;
        }

        switch (slot) {
            case SLOT_AA_TOGGLE:
                boolean currentStatus = plugin.getConfig().getBoolean("auto-announce.enabled");
                plugin.getConfig().set("auto-announce.enabled", !currentStatus);
                plugin.saveConfig();
                AutoAnnounce.stopAutoAnnounce();
                AutoAnnounce.startAutoAnnounce();
                openAutoAnnounceGUI(player);
                break;
            case SLOT_AA_INTERVAL:
                player.closeInventory();
                int currentInterval = plugin.getConfig().getInt("auto-announce.interval");
                player.sendMessage(ChatColor.YELLOW + "Please type the new interval (in seconds) in chat.");
                player.sendMessage(ChatColor.GRAY + "Current: " + currentInterval);
                player.sendMessage(ChatColor.GRAY + "(Type 'cancel' to exit)");
                autoAnnounceCreators.put(player.getUniqueId(), new HashMap<>() {{ put("step", STEP_INTERVAL); }});
                break;
            case SLOT_AA_MODE:
                String currentMode = plugin.getConfig().getString("auto-announce.mode");
                String newMode = currentMode.equalsIgnoreCase("ORDERED") ? "RANDOM" : "ORDERED";
                plugin.getConfig().set("auto-announce.mode", newMode);
                plugin.saveConfig();
                AutoAnnounce.stopAutoAnnounce();
                AutoAnnounce.startAutoAnnounce();
                openAutoAnnounceGUI(player);
                break;
            case SLOT_ADD_ITEM:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please type the name for the new auto-announce message in chat. " + ChatColor.GRAY + "(Type 'cancel' to exit)");
                autoAnnounceCreators.put(player.getUniqueId(), new HashMap<>() {{ put("step", STEP_NAME); }});
                break;
            default:
                if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null && slot < SLOT_BACK_BUTTON) {
                    String messageName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (event.isRightClick()) {
                        plugin.getConfig().set("auto-announce.messages." + messageName, null);
                        plugin.saveConfig();
                        AutoAnnounce.stopAutoAnnounce();
                        AutoAnnounce.startAutoAnnounce();
                        player.sendMessage(ChatColor.GREEN + "Auto-announce message '" + messageName + "' has been deleted.");
                        openAutoAnnounceGUI(player);
                    } else if (event.isLeftClick()) {
                        player.closeInventory();
                        String currentMessage = plugin.getConfig().getString("auto-announce.messages." + messageName + ".message");
                        player.sendMessage(ChatColor.YELLOW + "Please type the new message content for '" + messageName + "'. (Use | for a new line)");
                        player.sendMessage(ChatColor.GRAY + "Current: " + currentMessage);
                        player.sendMessage(ChatColor.GRAY + "(Type 'cancel' to exit)");
                        autoAnnounceEditors.put(player.getUniqueId(), new HashMap<>() {{ put("name", messageName); put("step", STEP_MESSAGE); }});
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (isNavigating.contains(playerUUID) || presetCreators.containsKey(playerUUID) || presetEditors.containsKey(playerUUID) || autoAnnounceCreators.containsKey(playerUUID) || autoAnnounceEditors.containsKey(playerUUID)) {
            return;
        }

        String presetsGUITitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title"));
        String autoAnnounceGUITitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.auto-announce-gui-title"));
        String closedGUITitle = event.getView().getTitle();

        if (closedGUITitle.equals(presetsGUITitle) || closedGUITitle.equals(autoAnnounceGUITitle)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> openEditGUI(player), 1L);
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            if (presetCreators.containsKey(playerUUID) || presetEditors.containsKey(playerUUID) || autoAnnounceCreators.containsKey(playerUUID) || autoAnnounceEditors.containsKey(playerUUID)) {
                event.setCancelled(true);
                presetCreators.remove(playerUUID);
                presetEditors.remove(playerUUID);
                autoAnnounceCreators.remove(playerUUID);
                autoAnnounceEditors.remove(playerUUID);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.input-cancelled")));
                return;
            }
        }

        if (presetCreators.containsKey(playerUUID)) {
            event.setCancelled(true);
            if (presetCreators.get(playerUUID).equals(STEP_NAME)) {
                if (message.contains(".")) {
                    player.sendMessage(ChatColor.RED + "The preset name cannot contain a period (.). Please choose another name.");
                    return;
                }
                presetCreators.put(playerUUID, message);
                player.sendMessage(ChatColor.GREEN + "Preset name set to '" + message + "'. Now, please type the message for this preset. (Use | for a new line) " + ChatColor.GRAY + "(Type 'cancel' to exit)");
            } else {
                String presetName = presetCreators.get(playerUUID);
                presetCreators.remove(playerUUID);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getConfig().set("presets." + presetName, message);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Preset '" + presetName + "' has been added successfully!");
                });
            }
        } else if (presetEditors.containsKey(playerUUID)) {
            event.setCancelled(true);
            String presetName = presetEditors.get(playerUUID);
            presetEditors.remove(playerUUID);
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getConfig().set("presets." + presetName, message);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Preset '" + presetName + "' has been updated successfully!");
            });
        } else if (autoAnnounceCreators.containsKey(playerUUID)) {
            event.setCancelled(true);
            Map<String, String> creatorData = autoAnnounceCreators.get(playerUUID);
            String step = creatorData.get("step");

            switch (step) {
                case STEP_INTERVAL:
                    try {
                        int interval = Integer.parseInt(message);
                        autoAnnounceCreators.remove(playerUUID);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getConfig().set("auto-announce.interval", interval);
                            plugin.saveConfig();
                            AutoAnnounce.stopAutoAnnounce();
                            AutoAnnounce.startAutoAnnounce();
                            player.sendMessage(ChatColor.GREEN + "Interval has been set to " + interval + " seconds.");
                        });
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid number. Please type a valid interval in seconds.");
                    }
                    break;
                case STEP_NAME:
                    if (message.contains(".")) {
                        player.sendMessage(ChatColor.RED + "The message name cannot contain a period (.). Please choose another name.");
                        return;
                    }
                    creatorData.put("name", message);
                    creatorData.put("step", STEP_MESSAGE);
                    player.sendMessage(ChatColor.GREEN + "Name set to '" + message + "'. Now, please type the message content. (Use | for a new line) " + ChatColor.GRAY + "(Type 'cancel' to exit)");
                    break;
                case STEP_MESSAGE:
                    creatorData.put("message", message);
                    creatorData.put("step", STEP_STYLE);
                    player.sendMessage(ChatColor.GREEN + "Message content set. Now, please type the style (GOAL, TASK, or CHALLENGE). " + ChatColor.GRAY + "(Type 'cancel' to exit)");
                    break;
                case STEP_STYLE:
                    if (message.equalsIgnoreCase("GOAL") || message.equalsIgnoreCase("TASK") || message.equalsIgnoreCase("CHALLENGE")) {
                        creatorData.put("style", message.toUpperCase());
                        creatorData.put("step", STEP_ICON);
                        player.sendMessage(ChatColor.GREEN + "Style set to '" + message.toUpperCase() + "'. Now, please type the icon material name. " + ChatColor.GRAY + "(Type 'cancel' to exit)");
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid style. Please type GOAL, TASK, or CHALLENGE.");
                    }
                    break;
                case STEP_ICON:
                    try {
                        Material.valueOf(message.toUpperCase());
                        creatorData.put("icon", message.toUpperCase());
                        autoAnnounceCreators.remove(playerUUID);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            String name = creatorData.get("name");
                            plugin.getConfig().set("auto-announce.messages." + name + ".message", creatorData.get("message"));
                            plugin.getConfig().set("auto-announce.messages." + name + ".style", creatorData.get("style"));
                            plugin.getConfig().set("auto-announce.messages." + name + ".icon", creatorData.get("icon"));
                            plugin.saveConfig();
                            AutoAnnounce.stopAutoAnnounce();
                            AutoAnnounce.startAutoAnnounce();
                            player.sendMessage(ChatColor.GREEN + "Auto-announce message '" + name + "' has been added successfully!");
                        });
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Invalid material name. Please try again.");
                    }
                    break;
            }
        } else if (autoAnnounceEditors.containsKey(playerUUID)) {
            event.setCancelled(true);
            Map<String, String> editorData = autoAnnounceEditors.get(playerUUID);
            String name = editorData.get("name");
            String step = editorData.get("step");
            String currentStyle = plugin.getConfig().getString("auto-announce.messages." + name + ".style");
            String currentIcon = plugin.getConfig().getString("auto-announce.messages." + name + ".icon");


            switch (step) {
                case STEP_MESSAGE:
                    editorData.put("message", message);
                    editorData.put("step", STEP_STYLE);
                    player.sendMessage(ChatColor.GREEN + "Message content updated. Now, please type the new style (GOAL, TASK, or CHALLENGE).");
                    player.sendMessage(ChatColor.GRAY + "Current: " + currentStyle);
                    player.sendMessage(ChatColor.GRAY + "(Type 'cancel' to exit)");
                    break;
                case STEP_STYLE:
                    if (message.equalsIgnoreCase("GOAL") || message.equalsIgnoreCase("TASK") || message.equalsIgnoreCase("CHALLENGE")) {
                        editorData.put("style", message.toUpperCase());
                        editorData.put("step", STEP_ICON);
                        player.sendMessage(ChatColor.GREEN + "Style updated to '" + message.toUpperCase() + "'. Now, please type the new icon material name.");
                        player.sendMessage(ChatColor.GRAY + "Current: " + currentIcon);
                        player.sendMessage(ChatColor.GRAY + "(Type 'cancel' to exit)");
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid style. Please type GOAL, TASK, or CHALLENGE.");
                    }
                    break;
                case STEP_ICON:
                    try {
                        Material.valueOf(message.toUpperCase());
                        autoAnnounceEditors.remove(playerUUID);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.getConfig().set("auto-announce.messages." + name + ".message", editorData.get("message"));
                            plugin.getConfig().set("auto-announce.messages." + name + ".style", editorData.get("style"));
                            plugin.getConfig().set("auto-announce.messages." + name + ".icon", message.toUpperCase());
                            plugin.saveConfig();
                            AutoAnnounce.stopAutoAnnounce();
                            AutoAnnounce.startAutoAnnounce();
                            player.sendMessage(ChatColor.GREEN + "Auto-announce message '" + name + "' has been updated successfully!");
                        });
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Invalid material name. Please try again.");
                    }
                    break;
            }
        }
    }
}