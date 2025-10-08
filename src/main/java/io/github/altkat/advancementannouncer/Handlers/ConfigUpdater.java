package io.github.altkat.advancementannouncer.Handlers;

import com.google.common.base.Charsets;
import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ConfigUpdater {

    public static void update(AdvancementAnnouncer plugin) throws IOException {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml"), Charsets.UTF_8));
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(configFile);

        userConfig.options().copyHeader(true);
        userConfig.options().header(defaultConfig.options().header());

        for (String key : defaultConfig.getKeys(true)) {
            if (!userConfig.contains(key)) {
                userConfig.set(key, defaultConfig.get(key));
            }
        }

        userConfig.save(configFile);
    }
}