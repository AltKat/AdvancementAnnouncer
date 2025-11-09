package io.github.altkat.advancementannouncer;
import io.github.altkat.advancementannouncer.Handlers.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

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
        getCommand("advancementannouncer").setTabCompleter(commandHandler);

        getServer().getPluginManager().registerEvents(new GUIHandler(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        AutoAnnounce.startAutoAnnounce();

        int pluginId = 24282;
        new Metrics(this, pluginId);


        new UpdateChecker(this, "altkat/AdvancementAnnouncer").checkAsync();

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