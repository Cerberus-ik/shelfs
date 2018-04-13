package de.treona.musicPlugin.config;

import de.treona.shelfs.api.Shelfs;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONException;
import org.json.JSONObject;

public class GuildSettings {

    public Role djRole;
    public Role settingsRole;
    public int volume;
    public String autoPlaylist;
    public int maxSongLength;
    public TextChannel musicChannel;

    private GuildSettings(Role djRole, Role settingsRole, int volume, String autoPlaylist, int maxSongLength, TextChannel channel) {
        this.djRole = djRole;
        this.settingsRole = settingsRole;
        this.volume = volume;
        this.autoPlaylist = autoPlaylist;
        this.maxSongLength = maxSongLength;
        this.musicChannel = channel;
    }

    static GuildSettings fromJSON(JSONObject guildSettingsObject) {
        Role djRole;
        Role settingsRole;
        try {
            djRole = Shelfs.getJda().getRoleById(guildSettingsObject.getString("botRole"));
        } catch (IllegalArgumentException | NullPointerException | JSONException e) {
            djRole = null;
        }
        try {
            settingsRole = Shelfs.getJda().getRoleById(guildSettingsObject.getString("settingsRole"));
        } catch (IllegalArgumentException | NullPointerException | JSONException e) {
            settingsRole = null;
        }

        TextChannel textChannel;
        try {
            textChannel = Shelfs.getJda().getTextChannelById(guildSettingsObject.getString("musicChannel"));
        } catch (IllegalArgumentException | JSONException e) {
            textChannel = null;
        }
        return new GuildSettings(djRole, settingsRole,
                guildSettingsObject.getInt("volume"),
                guildSettingsObject.getString("autoPlaylist"),
                guildSettingsObject.getInt("maxSongLength"),
                textChannel);
    }

    static GuildSettings newGuildSettings() {
        return new GuildSettings(null, null, 35, null, 900, null);
    }

    JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        if (this.djRole == null)
            jsonObject.put("botRole", "");
        else
            jsonObject.put("botRole", this.djRole.getId());
        if (this.settingsRole == null)
            jsonObject.put("settingsRole", "");
        else
            jsonObject.put("settingsRole", this.settingsRole.getId());
        if (this.musicChannel == null)
            jsonObject.put("musicChannel", "");
        else
            jsonObject.put("musicChannel", this.musicChannel.getId());

        jsonObject.put("autoPlaylist", this.requireNotNullOrElse(this.autoPlaylist, ""));
        jsonObject.put("volume", this.volume);
        jsonObject.put("maxSongLength", this.maxSongLength);
        return jsonObject;
    }

    @SuppressWarnings("SameParameterValue")
    private String requireNotNullOrElse(String targetString, String alternative) {
        if (targetString == null) {
            return alternative;
        }
        return targetString;
    }
}
