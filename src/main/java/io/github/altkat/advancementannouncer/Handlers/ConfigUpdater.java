package io.github.altkat.advancementannouncer.Handlers;

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

        // Step 1: Migrate very old string-only presets
        migratePresets(plugin);

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("config.yml"), Charsets.UTF_8)
        );
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(configFile);

        userConfig.options().copyHeader(true);
        userConfig.options().header(defaultConfig.options().header());

        // Step 2: Add new top-level keys
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

        // Step 3: Add 'custom-model-data' field to all existing messages
        addMissingCmdField(userConfig.getConfigurationSection("presets"));
        addMissingCmdField(userConfig.getConfigurationSection("auto-announce.messages"));
        addMissingCmdField(userConfig.getConfigurationSection("join-features.join-messages.messages"));
        addMissingCmdField(userConfig.getConfigurationSection("join-features.first-join-messages.messages"));

        // Step 4: Ensure base sections exist
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

    /**
     * Iterates over a message section and adds 'custom-model-data: ""' if it's missing.
     */
    private static void addMissingCmdField(ConfigurationSection section) {
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                ConfigurationSection messageConfig = section.getConfigurationSection(key);

                if (messageConfig != null && !messageConfig.isSet("custom-model-data")) {
                    messageConfig.set("custom-model-data", "");
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

                AdvancementAnnouncer.log("&aMigrated legacy preset to new format: " + key);
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
            AdvancementAnnouncer.log("&aCreated backup of config.yml before migration.");
        } catch (IOException e) {
            AdvancementAnnouncer.log("&cFailed to create config backup: " + e.getMessage());
        }
    }
}