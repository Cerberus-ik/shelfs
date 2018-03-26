package de.treona.basePlugin.config;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import org.json.JSONObject;

public class ConfigManager {

    private ShelfsPlugin shelfsPlugin;

    public ConfigManager(ShelfsPlugin shelfsPlugin) {
        this.shelfsPlugin = shelfsPlugin;
    }

    public JSONObject loadConfig() {
        JSONObject config = this.shelfsPlugin.getConfig();
        if (config == null) {
            config = this.shelfsPlugin.getDefaultConfig();
            this.shelfsPlugin.saveConfig(config);
        }
        return config;
    }

    public void saveConfig(JSONObject config) {
        this.shelfsPlugin.saveConfig(config);
    }
}
