package io.github.altkat.advancementannouncer.util;

import com.google.common.base.Charsets;
import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigUpdater {

    public static void update(AdvancementAnnouncer plugin) throws IOException {

        migratePresets(plugin);

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("config.yml"), Charsets.UTF_8)
        );
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(configFile);

        userConfig.options().copyHeader(true);
        userConfig.options().header(defaultConfig.options().header());

        for (String key : defaultConfig.getKeys(true)) {
            if (key.startsWith("presets.") ||
                    key.startsWith("auto-announce.messages.") ||
                    key.startsWith("join-features.join-messages.messages.") ||
                    key.startsWith("join-features.first-join-messages.messages.")) {
                continue;
            }

            if (!userConfig.contains(key)) {
                userConfig.set(key, defaultConfig.get(key));
            }
        }

        if (userConfig.isSet("lang-messages.edit-gui-title")) {
            userConfig.set("lang-messages.edit-gui-title", null);
        }
        if (userConfig.isSet("lang-messages.presets-gui-title")) {
            userConfig.set("lang-messages.presets-gui-title", null);
        }
        if (userConfig.isSet("lang-messages.auto-announce-gui-title")) {
            userConfig.set("lang-messages.auto-announce-gui-title", null);
        }
        if (userConfig.isSet("lang-messages.input-cancelled")) {
            userConfig.set("lang-messages.input-cancelled", null);
        }

        addMissingMessageFields(userConfig.getConfigurationSection("presets"));
        addMissingMessageFields(userConfig.getConfigurationSection("auto-announce.messages"));
        addMissingMessageFields(userConfig.getConfigurationSection("join-features.join-messages.messages"));
        addMissingMessageFields(userConfig.getConfigurationSection("join-features.first-join-messages.messages"));

        if (!userConfig.contains("presets")) {
            userConfig.createSection("presets");
        }
        if (!userConfig.contains("auto-announce.messages")) {
            userConfig.createSection("auto-announce.messages");
        }
        if (!userConfig.contains("join-features.join-messages.messages")) {
            userConfig.createSection("join-features.join-messages.messages");
        }
        if (!userConfig.contains("join-features.first-join-messages.messages")) {
            userConfig.createSection("join-features.first-join-messages.messages");
        }

        userConfig.save(configFile);
    }

    private static void addMissingMessageFields(ConfigurationSection section) {
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                ConfigurationSection messageConfig = section.getConfigurationSection(key);

                if (messageConfig != null) {
                    if (!messageConfig.isSet("custom-model-data")) {
                        messageConfig.set("custom-model-data", "");
                    }
                    if (!messageConfig.isSet("sound")) {
                        messageConfig.set("sound", "");
                    }
                }
            }
        }
    }

    /**
     * Migrates legacy string-only presets.
     */
    private static void migratePresets(AdvancementAnnouncer plugin) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection presets = config.getConfigurationSection("presets");

        if (presets == null) return;

        boolean migrated = false;
        boolean backupCreated = false;

        for (String key : presets.getKeys(false)) {
            if (presets.isString(key)) {
                if (!backupCreated) {
                    createBackup(plugin);
                    backupCreated = true;
                }

                String oldMessage = presets.getString(key);
                presets.set(key, null);

                ConfigurationSection newSection = presets.createSection(key);
                newSection.set("message", oldMessage);
                newSection.set("style", "GOAL");
                newSection.set("icon", "PAPER");
                newSection.set("custom-model-data", "");

                AdvancementAnnouncer.log("&#FCD05CMigrated legacy preset to new format: " + key);
                migrated = true;
            }
        }

        if (migrated) {
            plugin.saveConfig();
            plugin.reloadConfig();
        }
    }

    /**
     * Creates a backup of the config.yml file.
     */
    private static void createBackup(AdvancementAnnouncer plugin) {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File backupFile = new File(plugin.getDataFolder(), "config.yml.backup");

            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            AdvancementAnnouncer.log("&#FCD05CCreated backup of config.yml before migration.");
        } catch (IOException e) {
            AdvancementAnnouncer.log("&#F86B6BFailed to create config backup: " + e.getMessage());
        }
    }
}