package io.github.altkat.advancementannouncer;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;

import io.github.altkat.advancementannouncer.Handlers.CustomModelDataResolver;
import io.github.altkat.advancementannouncer.Handlers.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class AdvancementAnnouncer extends JavaPlugin {
    boolean IsPAPIEnabled;
    int version;
    int minorVersion;

    private CustomModelDataResolver cmdResolver;

    private AdvancementMain advancementMain;
    private UltimateAdvancementAPI ultimateAdvancementAPI;

    private boolean useApi = false;
    private String prefix = null;

    /**
     * Sends a colored message to the console.
     * @param message The message to send (supports '&' color codes).
     */
    public static void log(String message) {
        AdvancementAnnouncer plugin = AdvancementAnnouncer.getInstance();
        String prefix = "&3[AdvancementAnnouncer] &r";
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }


    @Override
    public void onLoad() {
        try {
            advancementMain = new AdvancementMain(this);
            advancementMain.load();
        } catch (Exception e) {
            log("&c############################################################");
            log("&cCRITICAL: Failed to LOAD shaded UltimateAdvancementAPI!");
            log("&cPlugin may not function correctly.");
            e.printStackTrace();
            log("&c############################################################");
        }
    }

    @Override
    public void onEnable() {

        updateConfig();

        final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        final String versionString = bukkitVersion.split("\\-")[0];
        final String[] versions = versionString.split("\\.");
        version = Integer.parseInt(versions[1]);
        minorVersion = (versions.length > 2) ? Integer.parseInt(versions[2].split("-")[0]) : 0;

        if(version < 16){
            log("&cThis plugin is only compatible with 1.16 and above!, disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        cmdResolver = new CustomModelDataResolver(this);

        try {
            if (advancementMain == null) {
                log("&eAdvancementMain was null onEnable, attempting reload...");
                advancementMain = new AdvancementMain(this);
                advancementMain.load();
            }

            advancementMain.enable(() -> new SQLite(advancementMain, new File(getDataFolder(), "database.db")));

            ultimateAdvancementAPI = UltimateAdvancementAPI.getInstance(this);
            useApi = true;

            log("&aCustomModelData support is ENABLED (API is shaded).");
            cmdResolver.detectExternalPlugins();

        } catch (Exception e) {
            log("&c############################################################");
            log("&cCRITICAL: Failed to ENABLE shaded UltimateAdvancementAPI!");
            log("&cCustomModelData support will be DISABLED.");
            e.printStackTrace();
            log("&c############################################################");
            useApi = false;
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            IsPAPIEnabled = true;
            log("&aPlaceholderAPI found! Enabling placeholder support...");
        }

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

        log("&aPlugin has been enabled!");
    }

    private void updateConfig() {
        saveDefaultConfig();
        try {
            ConfigUpdater.update(this);
            reloadConfig();
            clearPrefixCache();
        } catch (IOException e) {
            log("&cCould not update config.yml!");
            e.printStackTrace();
        }
    }


    @Override
    public void onDisable() {
        if (advancementMain != null) {
            advancementMain.disable();
        }

        log("&cPlugin has been disabled!");
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

    public boolean isModernVersion() {
        if (version > 20) return true;
        if (version == 20 && minorVersion >= 5) return true;
        return false;
    }

    public CustomModelDataResolver getCmdResolver() {
        return cmdResolver;
    }

    public UltimateAdvancementAPI getAdvancementAPI() {
        return ultimateAdvancementAPI;
    }

    public boolean isApiAvailable() {
        return useApi;
    }

    public String getPrefix() {
        if (this.prefix == null) {
            this.prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("lang-messages.plugin-prefix", "&3[AdvancementAnnouncer] &r"));
        }
        return this.prefix;
    }

    public void clearPrefixCache() {
        this.prefix = null;
    }
}