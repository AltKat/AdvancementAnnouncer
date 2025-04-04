package io.github.altkat.advancementannouncer;
import io.github.altkat.advancementannouncer.Handlers.AutoAnnounce;
import io.github.altkat.advancementannouncer.Handlers.CommandHandler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class AdvancementAnnouncer extends JavaPlugin {
    FileConfiguration config = getConfig();
    boolean IsPAPIEnabled;
    int version;
    @Override
    public void onEnable() {
        final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        final String versionString = bukkitVersion.split("\\-")[0];
        final String[] versions = versionString.split("\\.");

        version = Integer.parseInt(versions[1]);

        if(version < 16){
            getLogger().severe("This plugin is only compatible with 1.16 and above!");
            getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §cThis plugin is only compatible with 1.16 and above!, disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            IsPAPIEnabled = true;
            getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §aPlaceholderAPI found! Enabling placeholder support...");
        }

        loadConfig(); // ön yükleme

        File configFile = new File(getDataFolder(), "config.yml");
        try {
            io.github.altkat.advancementannouncer.Handlers.ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig(); // güncellenmiş configi tekrar yükle

        new PlayerData(this);

        getCommand("advancementannouncer").setExecutor(new CommandHandler());
        AutoAnnounce.startAutoAnnounce();

        if(config.getBoolean("bstats")) {
            int pluginId = 24282;
            Metrics metrics = new Metrics(this, pluginId);
        }

        getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §aPlugin has been enabled!");
    }

    @Override
    public void onDisable() {

        getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §cPlugin has been disabled!");
    }

    public static AdvancementAnnouncer getInstance() {
        return getPlugin(AdvancementAnnouncer.class);
    }

    public void loadConfig() {
        if (!getConfig().isSet("presets")) {
            saveDefaultConfig();
        }
        reloadConfig();
    }

    public boolean isPAPIEnabled() {
        return IsPAPIEnabled;
    }
    public int getVersion(){
        return version;
    }
}