package io.github.altkat.advancementannouncer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

public class PlayerData {

    private static File file;
    private static FileConfiguration config;
    private static AdvancementAnnouncer plugin;

    public PlayerData(AdvancementAnnouncer plugin) {
        PlayerData.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "data/playerdata.yml");
        if (!file.exists()) {
            plugin.saveResource("data/playerdata.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        if (config.getKeys(false).isEmpty()) {
            config.createSection("default");
            save();
        }
    }

    public static void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addPlayerData(UUID uuid) {
        config.createSection(uuid.toString());
        config.getConfigurationSection(uuid.toString()).set("toggleStatus", true);
        config.getConfigurationSection(uuid.toString()).set("playerName", plugin.getServer().getPlayer(uuid).getName());
        save();
    }

    public static void setToggleData(UUID uuid, boolean status) {
        config.getConfigurationSection(uuid.toString()).set("toggleStatus", status);
        save();
    }

    public static boolean returnToggleData(UUID uuid) {
        if(config.getConfigurationSection(uuid.toString()) == null) {
            addPlayerData(uuid);
        }
        return config.getConfigurationSection(uuid.toString()).getBoolean("toggleStatus");
    }

    public static void updatePlayerData(UUID uuid) {
        if (!config.getConfigurationSection(uuid.toString()).contains("toggleStatus")) {
            config.getConfigurationSection(uuid.toString()).set("toggleStatus", false);
        }
        save();
    }

    public static void reloadPlayerData() {
        try {
            if (!file.exists()) {
                plugin.saveResource("data/playerdata.yml", false);
            }
            config = YamlConfiguration.loadConfiguration(file);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

