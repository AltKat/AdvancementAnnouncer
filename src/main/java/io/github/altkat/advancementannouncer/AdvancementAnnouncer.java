package io.github.altkat.advancementannouncer;
import io.github.altkat.advancementannouncer.Handlers.AutoAnnounce;
import io.github.altkat.advancementannouncer.Handlers.CommandHandler;
import io.github.altkat.advancementannouncer.Handlers.ConfigUpdater;
import io.github.altkat.advancementannouncer.Handlers.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class AdvancementAnnouncer extends JavaPlugin {
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

        updateConfig();

        new PlayerData(this);

        CommandHandler commandHandler = new CommandHandler();
        getCommand("advancementannouncer").setExecutor(commandHandler);
        getServer().getPluginManager().registerEvents(commandHandler, this);

        AutoAnnounce.startAutoAnnounce();

        FileConfiguration config = getConfig();
        if(config.getBoolean("bstats")) {
            int pluginId = 24282;
            Metrics metrics = new Metrics(this, pluginId);
        }

        new UpdateChecker(this, 121602).getVersion(newVersion -> {
            if (UpdateChecker.isNewerVersion(this.getDescription().getVersion(), newVersion)) {
                getLogger().warning("There is a new update available for AdvancementAnnouncer! Version: " + newVersion);
                getLogger().warning("Download it from: https://www.spigotmc.org/resources/advancement-announcer.121602/");
            } else {
                getLogger().info("Plugin is up to date.");
            }
        });

        getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §aPlugin has been enabled!");
    }

    private void updateConfig() {
        saveDefaultConfig();
        try {
            ConfigUpdater.update(this);
            reloadConfig();
        } catch (IOException e) {
            getLogger().severe("Could not update config.yml!");
            e.printStackTrace();
        }
    }


    @Override
    public void onDisable() {

        getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §cPlugin has been disabled!");
    }

    public static AdvancementAnnouncer getInstance() {
        return getPlugin(AdvancementAnnouncer.class);
    }

    public boolean isPAPIEnabled() {
        return IsPAPIEnabled;
    }
    public int getVersion(){
        return version;
    }
}