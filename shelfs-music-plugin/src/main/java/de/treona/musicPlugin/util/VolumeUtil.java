package de.treona.musicPlugin.util;

import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.config.GuildSettings;

public class VolumeUtil {

    public static final int MAX_VOLUME = 125;
    public static final int MIN_VOLUME = 3;

    public static void setVolume(int volume, GuildMusicManager guildMusicManager, ConfigManager configManager) {
        int oldVolume = guildMusicManager.player.getVolume();
        int newVolume = Math.max(3, Math.min(volume, 125));
        if (oldVolume != newVolume) {
            guildMusicManager.player.setVolume(newVolume);
            GuildSettings guildSettings = configManager.getGuildSettings(guildMusicManager.getGuild());
            guildSettings.volume = newVolume;
            configManager.saveGuildSettings(guildMusicManager.getGuild(), guildSettings);
        }
    }

    static void louder(GuildMusicManager guildMusicManager, ConfigManager configManager) {
        setVolume(guildMusicManager.player.getVolume() + 15, guildMusicManager, configManager);
    }

    static void quieter(GuildMusicManager guildMusicManager, ConfigManager configManager) {
        setVolume(guildMusicManager.player.getVolume() - 15, guildMusicManager, configManager);
    }

}
