package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.guis.AutoAnnounceGUI;
import io.github.altkat.advancementannouncer.guis.EditorGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputListener implements Listener {
    private final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
    public static final Map<UUID, Map<String, Object>> activeSessions = new HashMap<>();

    public static final String STEP_NAME = "name";
    public static final String STEP_MESSAGE = "message";
    public static final String STEP_STYLE = "style";
    public static final String STEP_ICON = "icon";
    public static final String STEP_CUSTOM_MODEL_DATA = "custom_model_data";
    public static final String STEP_INTERVAL = "interval";

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!activeSessions.containsKey(playerUUID)) return;

        Map<String, Object> data = activeSessions.get(playerUUID);
        if (!data.containsKey("step")) return;

        event.setCancelled(true);
        String message = event.getMessage();

        final String prefix = plugin.getPrefix();

        if (message.equalsIgnoreCase("cancel")) {
            data.remove("step");
            player.sendMessage(prefix + ChatColor.RED + "Input cancelled.");

            if (!data.containsKey("type")) {
                activeSessions.remove(playerUUID);
                Bukkit.getScheduler().runTask(plugin, () -> AutoAnnounceGUI.open(player));
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> EditorGUI.open(player, data));
            }
            return;
        }

        String step = (String) data.get("step");
        Bukkit.getScheduler().runTask(plugin, () -> {
            switch (step) {
                case STEP_NAME:
                    if (message.contains(".") || message.contains(" ")) {
                        player.sendMessage(prefix + ChatColor.RED + "The name cannot contain periods or spaces. Please try again.");
                        return;
                    } else {
                        data.put("name", message);
                        player.sendMessage(prefix + ChatColor.GREEN + "Name set to '" + message + "'");
                    }
                    break;
                case STEP_MESSAGE:
                    data.put("message", message);
                    player.sendMessage(prefix + ChatColor.GREEN + "Message updated!");
                    break;
                case STEP_STYLE:
                    try {
                        AdvancementHandler.Style.valueOf(message.toUpperCase());
                        data.put("style", message.toUpperCase());
                        player.sendMessage(prefix + ChatColor.GREEN + "Style set to " + message.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(prefix + ChatColor.RED + "Invalid style! Please use GOAL, TASK, or CHALLENGE.");
                        return;
                    }
                    break;
                case STEP_ICON:
                    try {
                        Material.valueOf(message.toUpperCase());
                        data.put("icon", message.toUpperCase());
                        player.sendMessage(prefix + ChatColor.GREEN + "Icon set to " + message.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(prefix + ChatColor.RED + "Invalid material name! Please try again.");
                        return;
                    }
                    break;

                case STEP_CUSTOM_MODEL_DATA:
                    if (message.equalsIgnoreCase("none") || message.equalsIgnoreCase("0")) {
                        data.put("custom-model-data", "");
                        player.sendMessage(prefix + ChatColor.GREEN + "CustomModelData cleared.");
                    } else {
                        data.put("custom-model-data", message);
                        player.sendMessage(prefix + ChatColor.GREEN + "CustomModelData set to: " + message);
                    }
                    break;

                case STEP_INTERVAL:
                    try {
                        int interval = Integer.parseInt(message);
                        plugin.getConfig().set("auto-announce.interval", interval);
                        plugin.saveConfig();
                        AutoAnnounce.stopAutoAnnounce();
                        AutoAnnounce.startAutoAnnounce();
                        player.sendMessage(prefix + ChatColor.GREEN + "Interval has been set to " + interval + " seconds.");
                        activeSessions.remove(playerUUID);
                        AutoAnnounceGUI.open(player);
                        return;
                    } catch (NumberFormatException e) {
                        player.sendMessage(prefix + ChatColor.RED + "Invalid number. Please type a valid interval in seconds. Type 'cancel' to exit.");
                        return;
                    }
            }
            data.remove("step");
            EditorGUI.open(player, data);
        });
    }
}