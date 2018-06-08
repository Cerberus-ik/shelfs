package de.treona.shelfs.io.logger;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
public class Logger {

    private org.slf4j.Logger logger;

    public Logger(String moduleName) {
        this.logger = LoggerFactory.getLogger(moduleName);
    }

    public Logger(ShelfsPlugin plugin) {
        this(plugin.getPluginDescription().getName());
    }

    public Logger() {
        this("Shelfs");
    }

    public void logMessage(String message) {
        this.logger.info("Unknown: " + message);
    }

    public void logMessage(String message, LogLevel logLevel) {
        switch (logLevel) {
            case INFO:
                this.logger.info(message);
                break;
            case ERROR:
                this.logger.error(message);
                break;
            case UNKNOWN:
                this.logMessage(message);
                break;
            case WARNING:
                this.logger.warn(message);
                break;
        }
    }
}
