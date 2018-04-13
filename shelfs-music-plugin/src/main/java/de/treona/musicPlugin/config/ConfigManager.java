package de.treona.musicPlugin.config;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;

public class ConfigManager {

    private ShelfsPlugin shelfsPlugin;
    private JSONObject config;

    public ConfigManager(ShelfsPlugin shelfsPlugin) {
        this.shelfsPlugin = shelfsPlugin;
    }

    public JSONObject loadConfig() {
        this.config = this.shelfsPlugin.getConfig();
        if (this.config == null) {
            this.config = this.shelfsPlugin.getDefaultConfig();
            this.shelfsPlugin.saveConfig(config);
        }
        return this.config;
    }

    public JSONObject getConfig() {
        return config;
    }

    public GuildSettings getGuildSettings(Guild guild) {
        if (!this.config.getJSONObject("guildSettings").has(guild.getId())) {
            this.config.getJSONObject("guildSettings").put(guild.getId(), GuildSettings.newGuildSettings().toJSON());
            this.saveConfig();
        }
        return GuildSettings.fromJSON(this.config.getJSONObject("guildSettings").getJSONObject(guild.getId()));
    }

    public void saveGuildSettings(Guild guild, GuildSettings guildSettings) {
        this.config.getJSONObject("guildSettings").put(guild.getId(), guildSettings.toJSON());
        this.saveConfig();
    }

    private void saveConfig() {
        this.shelfsPlugin.saveConfig(this.config);
    }
}