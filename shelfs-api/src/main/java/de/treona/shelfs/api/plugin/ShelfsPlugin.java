package de.treona.shelfs.api.plugin;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.logger.Logger;
import de.treona.shelfs.io.resource.ResourceLoader;
import org.json.JSONObject;

import java.io.File;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ShelfsPlugin {

    public Logger logger;
    public PluginDescription pluginDescription;

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
        ResourceLoader resourceLoader = new ResourceLoader(this.getClass());
        return new JSONObject(resourceLoader.getResourceFileContent("config.json"));
    }

    public JSONObject getConfig() {
        return Shelfs.getConfig(this);
    }

    public void saveConfig(JSONObject config) {
        Shelfs.saveConfig(this, config);
    }

    public void writeDefaultConfig() {

    }
}
