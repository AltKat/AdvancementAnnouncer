package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker implements Listener {

    private final AdvancementAnnouncer plugin;
    private final String githubRepo;
    private String latestVersion;
    private static final String SPIGOT_URL = "https://www.spigotmc.org/resources/advancement-announcer.121602/";
    private final String GITHUB_REPO_URL;

    private static final long CONSOLE_LOG_DELAY = 30L;
    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/releases/latest";
    private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\":\"([^\"]+)\"");

    public UpdateChecker(AdvancementAnnouncer plugin, String githubRepo) {
        this.plugin = plugin;
        this.githubRepo = githubRepo;
        this.GITHUB_REPO_URL = "https://github.com/" + githubRepo + "/releases/latest";
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void checkAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            HttpURLConnection connection = null;
            try {
                String apiUrl = String.format(GITHUB_API_URL, this.githubRepo);
                connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setRequestProperty("User-Agent", "AdvancementAnnouncer-UpdateChecker");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

                int statusCode = connection.getResponseCode();
                if (statusCode != 200) {
                    plugin.getLogger().warning("GitHub API returned status: " + statusCode);
                    return;
                }

                String jsonResponse;
                try (InputStream inputStream = connection.getInputStream();
                     Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                    jsonResponse = scanner.useDelimiter("\\A").next();
                }

                Matcher tagMatcher = TAG_PATTERN.matcher(jsonResponse);
                if (tagMatcher.find()) {
                    this.latestVersion = tagMatcher.group(1);
                    final String currentVersion = plugin.getDescription().getVersion();

                    if (isNewerVersion(currentVersion, this.latestVersion)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &eA new update is available! Version: &a" + latestVersion));
                                plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDownload from Github: &6" + GITHUB_REPO_URL));
                                plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDownload from Spigot: &6" + SPIGOT_URL));
                            }
                        }.runTaskLater(plugin, CONSOLE_LOG_DELAY);
                    } else {
                        plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &aYou are using the latest version. (&e" + currentVersion + "&a)"));
                    }
                }
            } catch (IOException exception) {
                plugin.getLogger().warning("Unable to check for updates: " + exception.getMessage());
            } finally {
                if (connection != null) {
                    try {
                        connection.disconnect();
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("advancementannouncer.admin") && latestVersion != null) {
            if (isNewerVersion(plugin.getDescription().getVersion(), latestVersion)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[AdvancementAnnouncer] &eA new version is available: &a" + latestVersion));

                    TextComponent downloadFrom = new TextComponent(ChatColor.YELLOW + "Download from: ");

                    TextComponent githubLink = new TextComponent(ChatColor.GREEN + "[GitHub]");
                    githubLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, GITHUB_REPO_URL));
                    githubLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open GitHub page").color(net.md_5.bungee.api.ChatColor.GREEN).create()));

                    TextComponent separator = new TextComponent(ChatColor.GRAY + " | ");

                    TextComponent spigotLink = new TextComponent(ChatColor.GOLD + "[SpigotMC]");
                    spigotLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SPIGOT_URL));
                    spigotLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open SpigotMC page").color(net.md_5.bungee.api.ChatColor.GOLD).create()));

                    player.spigot().sendMessage(downloadFrom, githubLink, separator, spigotLink);
                }, 40L);
            }
        }
    }

    public static boolean isNewerVersion(String currentVersion, String latestVersion) {
        if (currentVersion == null || latestVersion == null) return false;
        String current = currentVersion.replaceAll("[vV]", "").split("-")[0].trim();
        String latest = latestVersion.replaceAll("[vV]", "").split("-")[0].trim();

        String[] currentParts = current.split("\\.");
        String[] latestParts = latest.split("\\.");

        int length = Math.max(currentParts.length, latestParts.length);
        for (int i = 0; i < length; i++) {
            try {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                if (latestPart > currentPart) return true;
                if (latestPart < currentPart) return false;
            } catch (NumberFormatException e) {
                return !current.equalsIgnoreCase(latest);
            }
        }
        return false;
    }
}