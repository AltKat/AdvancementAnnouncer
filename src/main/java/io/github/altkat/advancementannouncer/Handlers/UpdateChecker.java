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
import java.net.URLConnection;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker implements Listener {

    private final AdvancementAnnouncer plugin;
    private final int resourceId;
    private String latestVersion;
    private boolean updateCheckCompleted = false;

    public UpdateChecker(AdvancementAnnouncer plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            InputStream inputStream = null;
            Scanner scanner = null;
            try {
                URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.addRequestProperty("User-Agent", "AdvancementAnnouncer-UpdateChecker");

                inputStream = connection.getInputStream();
                scanner = new Scanner(inputStream);

                if (scanner.hasNext()) {
                    String version = scanner.next();
                    this.latestVersion = version;
                    this.updateCheckCompleted = true;
                    consumer.accept(version);
                } else {
                    this.plugin.getLogger().warning("Update checker received empty response from SpigotMC API");
                    this.updateCheckCompleted = true;
                }
            } catch (IOException exception) {
                this.plugin.getLogger().warning("Unable to check for updates: " + exception.getMessage());
                this.updateCheckCompleted = true;
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        this.plugin.getLogger().warning("Error closing input stream: " + e.getMessage());
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("advancementannouncer.admin")) {
            if (!updateCheckCompleted) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    checkAndNotifyPlayer(player);
                }, 40L);
                return;
            }

            checkAndNotifyPlayer(player);
        }
    }

    private void checkAndNotifyPlayer(Player player) {
        if (latestVersion != null && isNewerVersion(plugin.getDescription().getVersion(), latestVersion)) {
            player.sendMessage(ChatColor.YELLOW + "A new version of AdvancementAnnouncer is available! (" + latestVersion + ")");
            player.sendMessage(ChatColor.YELLOW + "Download it from: " + ChatColor.AQUA + "https://www.spigotmc.org/resources/advancementannouncer." + resourceId + "/");
        }
    }

    public static boolean isNewerVersion(String currentVersion, String latestVersion) {
        String current = currentVersion.replaceAll("[vV]", "");
        String latest = latestVersion.replaceAll("[vV]", "");

        String[] currentParts = current.split("\\.");
        String[] latestParts = latest.split("\\.");

        int length = Math.max(currentParts.length, latestParts.length);
        for (int i = 0; i < length; i++) {
            try {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

                if (latestPart > currentPart) {
                    return true;
                }
                if (latestPart < currentPart) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return !currentVersion.equalsIgnoreCase(latestVersion);
            }
        }
        return false;
    }
}