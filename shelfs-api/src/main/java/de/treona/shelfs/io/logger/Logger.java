package de.treona.shelfs.io.logger;

import de.treona.shelfs.api.plugin.ShelfsPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private String pluginName;

    public Logger(ShelfsPlugin plugin) {
        this.pluginName = plugin.getPluginDescription().getName();
    }

    public Logger() {
        this.pluginName = "Shelfs";
    }

    public void logMessage(String message) {
        logMessage(message, LogLevel.UNKNOWN);
    }

    public void logMessage(String message, LogLevel logLevel) {
        System.out.println(getMessagePrefix() + " [" + logLevel.getName() + "] [" + this.pluginName + "]: " + message);
    }

    private String getMessagePrefix() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[HH:mm:ss]");
        return simpleDateFormat.format(new Date());
    }
}
