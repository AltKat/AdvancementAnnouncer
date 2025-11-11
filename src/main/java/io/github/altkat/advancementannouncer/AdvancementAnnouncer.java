package io.github.altkat.advancementannouncer;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import io.github.altkat.advancementannouncer.Handlers.CustomModelDataResolver;

import io.github.altkat.advancementannouncer.Handlers.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class AdvancementAnnouncer extends JavaPlugin {
    boolean IsPAPIEnabled;
    int version;
    int minorVersion;

    private CustomModelDataResolver cmdResolver;
    private UltimateAdvancementAPI ultimateAdvancementAPI;
    private boolean useApi = false;

    @Override
    public void onEnable() {

        updateConfig();

        final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        final String versionString = bukkitVersion.split("\\-")[0];
        final String[] versions = versionString.split("\\.");

        version = Integer.parseInt(versions[1]);
        minorVersion = (versions.length > 2) ? Integer.parseInt(versions[2].split("-")[0]) : 0;

        if(version < 16){
            getLogger().severe("This plugin is only compatible with 1.16 and above!");
            getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §cThis plugin is only compatible with 1.16 and above!, disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getBoolean("enable-custom-model-support")) {
            if (Bukkit.getPluginManager().getPlugin("UltimateAdvancementAPI") != null) {
                try {
                    ultimateAdvancementAPI = UltimateAdvancementAPI.getInstance(this);
                    useApi = true;
                    getLogger().info("Successfully hooked into UltimateAdvancementAPI. CustomModelData support is ENABLED.");
                } catch (Exception e) {
                    getLogger().severe("UltimateAdvancementAPI is present but failed to initialize! Disabling CustomModelData support. Error: " + e.getMessage());
                    useApi = false;
                }
            } else {
                getLogger().warning("'enable-custom-model-support' is true in config, but 'UltimateAdvancementAPI' plugin was not found.");
                getLogger().warning("CustomModelData support will be DISABLED. Please install the API to use this feature.");
                useApi = false;
            }
        } else {
            getLogger().info("'enable-custom-model-support' is false. CustomModelData support is DISABLED.");
            useApi = false;
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            IsPAPIEnabled = true;
            getServer().getConsoleSender().sendMessage("§3[AdvancementAnnouncer] §aPlaceholderAPI found! Enabling placeholder support...");
        }

        new PlayerData(this);

        cmdResolver = new CustomModelDataResolver(this);
        cmdResolver.detectExternalPlugins();

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

    /**
     * Checks if the server is 1.20.5 or newer (for Item Components syntax).
     * This is used by the LEGACY advancement sender.
     * @return true if server version is 1.20.5 or higher.
     */
    public boolean isModernVersion() {
        if (version > 20) return true; // 1.21+
        if (version == 20 && minorVersion >= 5) return true; // 1.20.5 or 1.20.6
        return false;
    }

    /**
     * Gets the instance of the CustomModelData resolver.
     * @return CustomModelDataResolver instance.
     */
    public CustomModelDataResolver getCmdResolver() {
        return cmdResolver;
    }

    /**
     * Gets the loaded instance of the UltimateAdvancementAPI.
     * @return UltimateAdvancementAPI instance, or null if not loaded.
     */
    public UltimateAdvancementAPI getAdvancementAPI() {
        return ultimateAdvancementAPI;
    }

    /**
     * Checks if the API is configured AND loaded.
     * @return true if CMD support is active.
     */
    public boolean isApiAvailable() {
        return useApi;
    }
}