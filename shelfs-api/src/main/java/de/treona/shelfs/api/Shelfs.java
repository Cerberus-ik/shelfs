package de.treona.shelfs.api;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import de.treona.shelfs.api.events.CommandListener;
import de.treona.shelfs.api.events.ShelfsListenerAdapter;
import de.treona.shelfs.api.events.listener.ReactionMessageListener;
import de.treona.shelfs.api.plugin.PluginManager;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.CommandManager;
import de.treona.shelfs.config.ConfigManager;
import de.treona.shelfs.io.IOManager;
import de.treona.shelfs.io.IOType;
import de.treona.shelfs.io.database.MySQLIOManager;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.logger.Logger;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.*;

@SuppressWarnings("unused")
public final class Shelfs {

    private static JDA jda;
    private static Logger logger = new Logger();
    private static PluginManager pluginManager = new PluginManager();
    private static ConfigManager configManager;
    private static CommandManager commandManager = new CommandManager();
    private static IOManager ioManager;

    public static void loadConfig() {
        logger.logMessage("Loading the configuration file.", LogLevel.INFO);
        configManager = new ConfigManager();
        configManager.load();
        commandManager.setCommandPrefix(configManager.getConfig().commandPrefix);
        setupIOManager();
    }

    private static void setupIOManager() {
        IOType ioType = configManager.getConfig().ioType;
        switch (ioType) {
            case MySQL:
                ioManager = new MySQLIOManager(configManager.getConfig().databaseCredentials);
                break;
        }
        ioManager.setupEnvironment();
    }

    private static void loadPlugins() {
        logger.logMessage("Loading plugins...", LogLevel.INFO);
        pluginManager.loadPlugins(configManager.getConfig().pluginDirectory);
    }

    public static void start() {
        logger.logMessage("Starting...", LogLevel.INFO);
        try {
            setJda(new JDABuilder(AccountType.BOT)
                    .setToken(configManager.getConfig().token)
                    .addEventListener(new ShelfsListenerAdapter())
                    .addEventListener(new CommandListener(configManager.getConfig().commandPrefix))
                    .addEventListener(new ReactionMessageListener())
                    .setAudioSendFactory(new NativeAudioSendFactory())
                    .buildBlocking());
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
        loadPlugins();
        logger.logMessage("Finished startup sequence!", LogLevel.INFO);
    }

    public static IOManager getIoManager() {
        return ioManager;
    }

    public static PluginManager getPluginManager() {
        return pluginManager;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static String getVersion() {
        return "0.4.6";
    }

    public static JDA getJda() {
        return jda;
    }

    private static void setJda(JDA jda) {
        if (Shelfs.jda != null) {
            throw new UnsupportedOperationException("The jda object is already set.");
        }
        Shelfs.jda = jda;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static File getConfigDirectory(ShelfsPlugin plugin) {
        return new File(configManager.getConfig().pluginDirectory + "/" + plugin.getPluginDescription().getName());
    }

    public static JSONObject getConfig(ShelfsPlugin plugin) {
        File configFile = new File(getConfigDirectory(plugin) + "/config.json");
        if (!configFile.exists()) {
            return plugin.getDefaultConfig();
        }
        try {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile))) {
                StringBuilder stringBuilder = new StringBuilder();
                bufferedReader.lines().forEach(stringBuilder::append);
                return new JSONObject(stringBuilder.toString());
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveConfig(ShelfsPlugin plugin, JSONObject config) {
        File configDirectory = getConfigDirectory(plugin);
        if (!configDirectory.exists()) {
            if (configDirectory.mkdirs()) {
                plugin.getLogger().logMessage("Created the config directory.", LogLevel.INFO);
            } else {
                plugin.getLogger().logMessage("Could not create the config directory.", LogLevel.WARNING);
                return;
            }
        }
        File configFile = new File(getConfigDirectory(plugin) + "/config.json");
        if (!configFile.exists()) {
            try {
                if (configFile.createNewFile()) {
                    plugin.getLogger().logMessage("Created the config file.", LogLevel.INFO);
                } else {
                    plugin.getLogger().logMessage("Could not create the config file.", LogLevel.WARNING);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configFile))) {
            bufferedWriter.write(config.toString());
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
