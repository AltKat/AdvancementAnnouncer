package io.github.altkat.advancementannouncer.core;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import org.bukkit.configuration.ConfigurationSection;
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
        ConfigurationSection section = config.createSection(uuid.toString());
        section.set("toggleStatus", true);
        section.set("soundsEnabled", true);
        section.set("playerName", plugin.getServer().getPlayer(uuid).getName());
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
        checkAndAddDefaults(uuid);
        return config.getConfigurationSection(uuid.toString()).getBoolean("toggleStatus", true);
    }


    public static void setSoundsEnabled(UUID uuid, boolean status) {
        if(config.getConfigurationSection(uuid.toString()) == null) {
            addPlayerData(uuid);
        }
        config.getConfigurationSection(uuid.toString()).set("soundsEnabled", status);
        save();
    }

    public static boolean areSoundsEnabled(UUID uuid) {
        if(config.getConfigurationSection(uuid.toString()) == null) {
            addPlayerData(uuid);
        }
        checkAndAddDefaults(uuid);
        return config.getConfigurationSection(uuid.toString()).getBoolean("soundsEnabled", true);
    }


    private static void checkAndAddDefaults(UUID uuid) {
        if (config.getConfigurationSection(uuid.toString()) != null) {
            if (!config.getConfigurationSection(uuid.toString()).contains("soundsEnabled")) {
                config.getConfigurationSection(uuid.toString()).set("soundsEnabled", true);
                save();
            }
        }
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

