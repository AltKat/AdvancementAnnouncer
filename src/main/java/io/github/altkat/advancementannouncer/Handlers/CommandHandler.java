package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.PlayerData;
import io.github.altkat.advancementannouncer.Handlers.guis.ConfirmationGUI;
import io.github.altkat.advancementannouncer.Handlers.guis.IconSelectionGUI;
import io.github.altkat.advancementannouncer.Handlers.guis.StyleSelectionGUI;
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


    private static final int SLOT_SET_NAME = 10;
    private static final int SLOT_SET_MESSAGE = 13;
    private static final int SLOT_SET_STYLE = 16;
    private static final int SLOT_SET_ICON = 19;
    private static final int SLOT_SAVE = 49;


    private static final String STEP_NAME = "name";
    private static final String STEP_MESSAGE = "message";
    private static final String STEP_STYLE = "style";
    private static final String STEP_ICON = "icon";
    private static final String STEP_INTERVAL = "interval";

    private final Map<UUID, Map<String, Object>> activeSessions = new HashMap<>();
    private final Map<UUID, Runnable> confirmationActions = new HashMap<>();

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
            int sentCount = 0;
            for(Player player : sender.getServer().getOnlinePlayers()){
                if(!PlayerData.returnToggleData(player.getUniqueId())){
                    continue;
                }
                AdvancementHandler.displayTo(player, materialName.toLowerCase(), message, style);
                sentCount++;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aAdvancement message sent to " + sentCount + " player(s)"));
        }else{
            Player player = sender.getServer().getPlayer(audience);
            if(player == null){
                sender.sendMessage(ChatColor.RED + "Player not found: " + audience);
                return true;
            }
            AdvancementHandler.displayTo(player, materialName.toLowerCase(), message, style);
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

    private void addFormattedMessage(List<String> lore, String message) {
        if (message.contains("|")) {
            for (String line : message.split("\\|")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', message));
        }
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
                addFormattedMessage(lore, presetsSection.getString(key));
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

    private void openCreatorEditorGUI(Player player, Map<String, Object> data) {
        boolean isCreator = (boolean) data.get("isCreator");
        String type = (String) data.get("type");
        String title = isCreator ? "Creating new " + type : "Editing " + type + ": " + data.get("name");

        Inventory gui = Bukkit.createInventory(null, 54, title);

        activeSessions.put(player.getUniqueId(), data);

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
        gui.setItem(45, backItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String clickedGUITitle = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        boolean isRelevantGUI = Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.edit-gui-title")),
                ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title")),
                ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.auto-announce-gui-title")),
                "Select a Style",
                "Confirm Deletion"
        ).contains(clickedGUITitle) || clickedGUITitle.startsWith("Editing ") || clickedGUITitle.startsWith("Creating ") || clickedGUITitle.startsWith("Select an Icon");

        if (!isRelevantGUI) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }
        isNavigating.add(player.getUniqueId());

        if (clickedGUITitle.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.edit-gui-title")))) {
            if (event.getSlot() == SLOT_EDIT_AUTO_ANNOUNCE) {
                openAutoAnnounceGUI(player);
            } else if (event.getSlot() == SLOT_EDIT_PRESETS) {
                openPresetsGUI(player);
            }
        } else if (clickedGUITitle.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.presets-gui-title")))) {
            handlePresetsGUIClick(event);
        } else if (clickedGUITitle.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lang-messages.auto-announce-gui-title")))) {
            handleAutoAnnounceGUIClick(event);
        } else if (clickedGUITitle.startsWith("Editing ") || clickedGUITitle.startsWith("Creating ")) {
            handleCreatorEditorGUIClick(event);
        } else if (clickedGUITitle.equals("Select a Style")) {
            handleStyleSelectionGUIClick(event);
        } else if (clickedGUITitle.startsWith("Select an Icon")) {
            handleIconSelectionGUIClick(event);
        } else if (clickedGUITitle.equals("Confirm Deletion")) {
            handleConfirmationGUIClick(event);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> isNavigating.remove(player.getUniqueId()), 1L);
    }

    private void handleCreatorEditorGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Map<String, Object> data = activeSessions.get(player.getUniqueId());

        switch (event.getSlot()) {
            case SLOT_SET_NAME:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please type the new name in chat. (Type 'cancel' to abort)");
                player.sendMessage(ChatColor.GRAY + "Current value: " + data.get("name"));
                data.put("step", STEP_NAME);
                break;
            case SLOT_SET_MESSAGE:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Please type the new message in chat. (Use | for new line, type 'cancel' to abort)");
                player.sendMessage(ChatColor.GRAY + "Current value: " + data.get("message"));
                data.put("step", STEP_MESSAGE);
                break;
            case SLOT_SET_STYLE:
                StyleSelectionGUI.open(player);
                break;
            case SLOT_SET_ICON:
                IconSelectionGUI.open(player, 0);
                break;
            case SLOT_SAVE:
                saveChanges(player);
                break;
            case 45:
                activeSessions.remove(player.getUniqueId());
                if (data != null) {
                    if (((String)data.get("type")).equals("preset")) {
                        openPresetsGUI(player);
                    } else {
                        openAutoAnnounceGUI(player);
                    }
                }
                break;
        }
    }

    private void handleStyleSelectionGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem.getType() == Material.WRITABLE_BOOK) {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Please type the style name (GOAL, TASK, CHALLENGE) in chat. (Type 'cancel' to abort)");
            activeSessions.get(player.getUniqueId()).put("step", STEP_STYLE);
            return;
        }

        if (clickedItem.getType() == Material.BARRIER) {
            Map<String, Object> data = activeSessions.get(player.getUniqueId());
            if (data != null) {
                openCreatorEditorGUI(player, data);
            }
            return;
        }

        String styleName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        Map<String, Object> data = activeSessions.get(player.getUniqueId());
        if (data != null) {
            data.put("style", styleName);
            player.sendMessage(ChatColor.GREEN + "Style set to " + styleName + "!");
            openCreatorEditorGUI(player, data);
        }
    }

    private void handleIconSelectionGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        String title = event.getView().getTitle();

        if (clickedItem == null) return;

        if (clickedItem.getType() == Material.WRITABLE_BOOK) {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Please type the material name for the icon in chat. (Type 'cancel' to abort)");
            activeSessions.get(player.getUniqueId()).put("step", STEP_ICON);
            return;
        }

        if (clickedItem.getType() == Material.BARRIER) {
            Map<String, Object> data = activeSessions.get(player.getUniqueId());
            if (data != null) {
                openCreatorEditorGUI(player, data);
            }
            return;
        }

        if (clickedItem.getType() == Material.ARROW) {
            String currentPageStr = title.substring(title.indexOf("Page ") + 5, title.indexOf("/"));
            int currentPage = Integer.parseInt(currentPageStr) - 1;
            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (itemName.equals("Next Page")) {
                IconSelectionGUI.open(player, currentPage + 1);
            } else if (itemName.equals("Previous Page")) {
                IconSelectionGUI.open(player, currentPage - 1);
            }
            return;
        }

        Material selectedMaterial = clickedItem.getType();
        Map<String, Object> data = activeSessions.get(player.getUniqueId());
        if (data != null) {
            data.put("icon", selectedMaterial.name());
            player.sendMessage(ChatColor.GREEN + "Icon set to " + selectedMaterial.name() + "!");
            openCreatorEditorGUI(player, data);
        }
    }

    private void handleConfirmationGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getSlot() == 11) {
            Runnable action = confirmationActions.remove(player.getUniqueId());
            if (action != null) {
                action.run();
            }
        } else if (event.getSlot() == 15) {
            confirmationActions.remove(player.getUniqueId());
            String rawLore = event.getInventory().getItem(13).getItemMeta().getLore().get(0);
            String itemName = ChatColor.stripColor(rawLore).replace("Delete '", "").replace("'?", "");
            if (plugin.getConfig().isSet("presets." + itemName)) {
                openPresetsGUI(player);
            } else {
                openAutoAnnounceGUI(player);
            }
        }
    }

    private void saveChanges(Player player) {
        Map<String, Object> data = activeSessions.get(player.getUniqueId());
        String name = (String) data.get("name");

        if (name == null || name.isBlank() || name.equals("<not set>")) {
            player.sendMessage(ChatColor.RED + "You must set a name before saving!");
            return;
        }
        if (name.contains(".") || name.contains(" ")) {
            player.sendMessage(ChatColor.RED + "The name cannot contain periods or spaces.");
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

        activeSessions.remove(player.getUniqueId());

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
            openPresetsGUI(player);
        } else {
            openAutoAnnounceGUI(player);
        }
    }

    private void handlePresetsGUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getSlot() == SLOT_BACK_BUTTON) {
            openEditGUI(player);
            return;
        }

        if (event.getSlot() == SLOT_ADD_ITEM) {
            Map<String, Object> data = new HashMap<>();
            data.put("isCreator", true);
            data.put("type", "preset");
            data.put("name", "<not set>");
            data.put("message", "Default message");
            openCreatorEditorGUI(player, data);
            return;
        }

        if (clickedItem.getType() == Material.PAPER) {
            String presetName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            if (event.isRightClick()) {
                ConfirmationGUI.open(player, presetName);
                confirmationActions.put(player.getUniqueId(), () -> {
                    plugin.getConfig().set("presets." + presetName, null);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Preset '" + presetName + "' has been deleted.");
                    openPresetsGUI(player);
                });
            } else if (event.isLeftClick()){
                Map<String, Object> data = new HashMap<>();
                data.put("isCreator", false);
                data.put("originalName", presetName);
                data.put("type", "preset");
                data.put("name", presetName);
                data.put("message", plugin.getConfig().getString("presets." + presetName));
                openCreatorEditorGUI(player, data);
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
                activeSessions.put(player.getUniqueId(), new HashMap<>() {{ put("step", STEP_INTERVAL); }});
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
                Map<String, Object> data = new HashMap<>();
                data.put("isCreator", true);
                data.put("type", "auto-announce");
                data.put("name", "<not set>");
                data.put("message", "Default message");
                data.put("style", "GOAL");
                data.put("icon", "STONE");
                openCreatorEditorGUI(player, data);
                return;
            default:
                if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null && slot < SLOT_BACK_BUTTON) {
                    String messageName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    if (event.isRightClick()) {
                        ConfirmationGUI.open(player, messageName);
                        confirmationActions.put(player.getUniqueId(), () -> {
                            plugin.getConfig().set("auto-announce.messages." + messageName, null);
                            plugin.saveConfig();
                            AutoAnnounce.stopAutoAnnounce();
                            AutoAnnounce.startAutoAnnounce();
                            player.sendMessage(ChatColor.GREEN + "Auto-announce message '" + messageName + "' has been deleted.");
                            openAutoAnnounceGUI(player);
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
                        openCreatorEditorGUI(player, editData);
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!activeSessions.containsKey(playerUUID)) return;

        Map<String, Object> data = activeSessions.get(playerUUID);
        String message = event.getMessage();

        if (data.containsKey("step")) {
            event.setCancelled(true);
            if (message.equalsIgnoreCase("cancel")) {
                data.remove("step");
                player.sendMessage(ChatColor.RED + "Input cancelled.");


                if (!data.containsKey("type")) {
                    activeSessions.remove(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () -> openAutoAnnounceGUI(player));
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> openCreatorEditorGUI(player, data));
                }
                return;
            }

            String step = (String) data.get("step");
            Bukkit.getScheduler().runTask(plugin, () -> {
                switch(step) {
                    case STEP_NAME:
                        if (message.contains(".") || message.contains(" ")) {
                            player.sendMessage(ChatColor.RED + "The name cannot contain periods or spaces. Please try again.");
                        } else {
                            data.put("name", message);
                            player.sendMessage(ChatColor.GREEN + "Name set to '" + message + "'");
                        }
                        break;
                    case STEP_MESSAGE:
                        data.put("message", message);
                        player.sendMessage(ChatColor.GREEN + "Message updated!");
                        break;
                    case STEP_STYLE:
                        try {
                            AdvancementHandler.Style.valueOf(message.toUpperCase());
                            data.put("style", message.toUpperCase());
                            player.sendMessage(ChatColor.GREEN + "Style set to " + message.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid style! Please use GOAL, TASK, or CHALLENGE.");
                        }
                        break;
                    case STEP_ICON:
                        try {
                            Material.valueOf(message.toUpperCase());
                            data.put("icon", message.toUpperCase());
                            player.sendMessage(ChatColor.GREEN + "Icon set to " + message.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + "Invalid material name! Please try again.");
                        }
                        break;
                    case STEP_INTERVAL:
                        try {
                            int interval = Integer.parseInt(message);
                            plugin.getConfig().set("auto-announce.interval", interval);
                            plugin.saveConfig();
                            AutoAnnounce.stopAutoAnnounce();
                            AutoAnnounce.startAutoAnnounce();
                            player.sendMessage(ChatColor.GREEN + "Interval has been set to " + interval + " seconds.");
                            activeSessions.remove(playerUUID);
                            openAutoAnnounceGUI(player);
                            return;
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid number. Please type a valid interval in seconds. Type 'cancel' to exit.");
                            return;
                        }
                }
                data.remove("step");
                openCreatorEditorGUI(player, data);
            });
        }
    }
}