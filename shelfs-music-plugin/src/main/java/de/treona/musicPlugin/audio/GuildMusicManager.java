package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import de.treona.musicPlugin.config.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;

public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    public final AudioPlayerSendHandler sendHandler;
    private Guild guild;

    GuildMusicManager(AudioPlayerManager manager, Guild guild, GuildSettings guildSettings, AudioController audioController) {
        this.guild = guild;
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, this, audioController, guildSettings.getAutoPlaylist(), guildSettings.getMusicChannel(), guild);
        sendHandler = new AudioPlayerSendHandler(player);
        player.addListener(scheduler);
    }

    public Guild getGuild() {
        return guild;
    }
}