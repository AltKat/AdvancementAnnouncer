package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import io.github.altkat.advancementannouncer.PlayerData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class JoinListener implements Listener {

    private final AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!PlayerData.returnToggleData(player.getUniqueId())) {
            return;
        }

        String basePath = "join-features." + (player.hasPlayedBefore() ? "join-messages" : "first-join-messages");

        if (!plugin.getConfig().getBoolean(basePath + ".enabled")) {
            return;
        }

        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection(basePath + ".messages");
        if (messagesSection == null) {
            return;
        }

        Set<String> keys = messagesSection.getKeys(false);
        if (keys.isEmpty()) {
            return;
        }

        List<String> keyList = new ArrayList<>(keys);
        String randomKey = keyList.get(ThreadLocalRandom.current().nextInt(keyList.size()));
        String messagePath = basePath + ".messages." + randomKey;

        String message = plugin.getConfig().getString(messagePath + ".message");
        String styleString = plugin.getConfig().getString(messagePath + ".style", "GOAL").toUpperCase();
        String iconString = plugin.getConfig().getString(messagePath + ".icon", "STONE").toUpperCase();

        String customModelDataString = plugin.getConfig().getString(messagePath + ".custom-model-data", null);

        try {
            AdvancementHandler.Style style = AdvancementHandler.Style.valueOf(styleString);
            Material icon = Material.valueOf(iconString);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    AdvancementHandler.displayTo(player, icon.name().toLowerCase(), customModelDataString, message, style);
                }
            }, 20L);

        } catch (IllegalArgumentException e) {
            AdvancementAnnouncer.log("&eInvalid style or icon in join messages configuration: " + randomKey);
        }
    }
}