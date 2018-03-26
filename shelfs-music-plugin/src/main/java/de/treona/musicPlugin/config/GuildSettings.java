package de.treona.musicPlugin.config;

import de.treona.shelfs.api.Shelfs;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONException;
import org.json.JSONObject;

public class GuildSettings {

    private Role botRole;
    private int volume;
    private String autoPlaylist;
    private int maxSongLength;
    private TextChannel musicChannel;

    private GuildSettings(Role botRole, int volume, String autoPlaylist, int maxSongLength, TextChannel channel) {
        this.botRole = botRole;
        this.volume = volume;
        this.autoPlaylist = autoPlaylist;
        this.maxSongLength = maxSongLength;
        this.musicChannel = channel;
    }

    static GuildSettings fromJSON(JSONObject guildSettingsObject) {
        Role role;
        try {
            role = Shelfs.getJda().getRoleById(guildSettingsObject.getString("botRole"));
        } catch (IllegalArgumentException e) {
            role = null;
        }
        TextChannel textChannel;
        try {
            textChannel = Shelfs.getJda().getTextChannelById(guildSettingsObject.getString("musicChannel"));
        } catch (IllegalArgumentException | JSONException e) {
            textChannel = null;
        }
        return new GuildSettings(role,
                guildSettingsObject.getInt("volume"),
                guildSettingsObject.getString("autoPlaylist"),
                guildSettingsObject.getInt("maxSongLength"),
                textChannel);
    }

    static GuildSettings newGuildSettings() {
        return new GuildSettings(null, 35, null, 900, null);
    }

    public Role getBotRole() {
        return botRole;
    }

    public void setBotRole(Role botRole) {
        this.botRole = botRole;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getAutoPlaylist() {
        return autoPlaylist;
    }

    public void setAutoPlaylist(String autoPlaylist) {
        this.autoPlaylist = autoPlaylist;
    }

    public TextChannel getMusicChannel() {
        return musicChannel;
    }

    public void setMusicChannel(TextChannel musicChannel) {
        this.musicChannel = musicChannel;
    }

    public int getMaxSongLength() {
        return maxSongLength;
    }

    public void setMaxSongLength(int maxSongLength) {
        this.maxSongLength = maxSongLength;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        if (this.botRole == null) {
            jsonObject.put("botRole", "");
        } else {
            jsonObject.put("botRole", this.botRole.getId());
        }
        if (this.musicChannel == null) {
            jsonObject.put("musicChannel", "");
        } else {
            jsonObject.put("musicChannel", this.musicChannel.getId());
        }
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
