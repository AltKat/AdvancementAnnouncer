package io.github.altkat.advancementannouncer;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.database.impl.SQLite;

import io.github.altkat.advancementannouncer.cmd.CustomModelDataResolver;
import io.github.altkat.advancementannouncer.core.PlayerData;
import io.github.altkat.advancementannouncer.editor.ChatInputListener;
import io.github.altkat.advancementannouncer.editor.GUIHandler;
import io.github.altkat.advancementannouncer.feature.AutoAnnounce;
import io.github.altkat.advancementannouncer.feature.CommandHandler;
import io.github.altkat.advancementannouncer.feature.JoinListener;
import io.github.altkat.advancementannouncer.util.ConfigUpdater;
import io.github.altkat.advancementannouncer.util.TextUtil;
import io.github.altkat.advancementannouncer.util.UpdateChecker;
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

    @Override
    public void onLoad() {
        try {
            advancementMain = new AdvancementMain(this);
            advancementMain.load();
        } catch (Exception e) {
            log("&e############################################################");
            log("&eWARNING: Failed to load UltimateAdvancementAPI (Shaded).");
            log("&eThis might be due to an unsupported server version.");
            log("&ePlugin will continue to run in legacy mode without CustomModelData support.");
            log("&e############################################################");
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

            File dataFolder = new File(getDataFolder(), "data");
            if (!dataFolder.exists()) {
                boolean created = dataFolder.mkdirs();
                if (!created) {
                    log("&c############################################################");
                    log("&cCRITICAL: Could not create the '/data/' folder.");
                    log("&cPlease check file permissions or delete any 'data' file.");
                    log("&cAPI will not be loaded. Falling back to legacy mode.");
                    log("&c############################################################");
                    throw new IOException("Failed to create data folder.");
                }
            }

            File databaseFile = new File(dataFolder, "uadb.db");
            advancementMain.enable(() -> new SQLite(advancementMain, databaseFile));

            ultimateAdvancementAPI = UltimateAdvancementAPI.getInstance(this);
            useApi = true;

            log("&aCustomModelData (CMD) support is active.");
            cmdResolver.detectExternalPlugins();

        } catch (Exception e) {
            log("&e############################################################");
            log("&eWARNING: Failed to enable UltimateAdvancementAPI (Shaded).");
            log("&eYour server version might be incompatible with this shaded version of UAPI.");
            log("&ePlugin is falling back to 'legacy' mode without CustomModelData support.");
            log("&e############################################################");
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

    public static void log(String message) {
        String prefix = "&#7688FF[Advancement Announcer] &r";
        Bukkit.getConsoleSender().sendMessage(TextUtil.color(prefix + message));
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
            String configPrefix = getConfig().getString("lang-messages.plugin-prefix", "&#7688FF[Advancement Announcer] &r");
            this.prefix = TextUtil.color(configPrefix);
        }
        return this.prefix;
    }

    public void clearPrefixCache() {
        this.prefix = null;
    }
}