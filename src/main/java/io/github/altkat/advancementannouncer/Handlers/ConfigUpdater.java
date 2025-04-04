package io.github.altkat.advancementannouncer.Handlers;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.List;

public class ConfigUpdater {

    public static void update(Plugin plugin, String resourceName, File toUpdate, List<String> ignoreSections) throws IOException {
        InputStream resourceStream = plugin.getResource(resourceName);
        if (resourceStream == null) {
            throw new FileNotFoundException("Resource '" + resourceName + "' not found inside plugin jar!");
        }

        YamlConfiguration defaultConfig = new YamlConfiguration();
        try {
            defaultConfig.load(new InputStreamReader(resourceStream));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }

        YamlConfiguration existingConfig = new YamlConfiguration();
        try {
            existingConfig.load(toUpdate);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }

        boolean changed = false;

        for (String key : defaultConfig.getKeys(true)) {
            if (ignoreSections != null && ignoreSections.stream().anyMatch(key::startsWith)) {
                continue; // Bu bölümü atla
            }

            if (!existingConfig.contains(key)) {
                existingConfig.set(key, defaultConfig.get(key));
                changed = true;
            }
        }

        if (changed) {
            existingConfig.save(toUpdate);
        }
    }
}
