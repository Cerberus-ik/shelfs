package de.treona.shelfs.api.plugin;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.logger.Logger;
import de.treona.shelfs.io.resource.JSONBeautifier;
import org.json.JSONObject;

import java.io.File;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ShelfsPlugin {

    public Logger logger;
    public PluginDescription pluginDescription;
    public JSONObject defaultConfig;

    public ShelfsPlugin() {
        this.logger = null;
        this.pluginDescription = null;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public Logger getLogger() {
        return this.logger;
    }

    public PluginDescription getPluginDescription() {
        return this.pluginDescription;
    }

    public File getConfigDirectory() {
        return Shelfs.getConfigDirectory(this);
    }

    public JSONObject getDefaultConfig() {
        return this.defaultConfig;
    }

    public JSONObject getConfig() {
        return Shelfs.getPluginConfig(this);
    }

    public void saveConfig(JSONObject config) {
        Shelfs.saveConfig(this, config);
    }

    public void writeDefaultConfig() {
        this.saveConfig(JSONBeautifier.beautifyJSONObjectToObject(this.getDefaultConfig()));
    }
}
