package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker implements Listener {

    private final AdvancementAnnouncer plugin;
    private final int resourceId;
    private String latestVersion;

    public UpdateChecker(AdvancementAnnouncer plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    String version = scanner.next();
                    this.latestVersion = version;
                    consumer.accept(version);
                }
            } catch (IOException exception) {
                this.plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("advancementannouncer.admin")) {
            if (latestVersion != null && !plugin.getDescription().getVersion().equalsIgnoreCase(latestVersion)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.sendMessage(ChatColor.YELLOW + "A new version of AdvancementAnnouncer is available! (" + latestVersion + ")");
                    player.sendMessage(ChatColor.YELLOW + "Download it from: " + ChatColor.AQUA + "https://www.spigotmc.org/resources/advancementannouncer." + resourceId + "/");
                }, 20L);
            }
        }
    }
}