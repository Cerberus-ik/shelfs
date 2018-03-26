package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.logger.LogLevel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler extends AudioEventAdapter {

    public final Queue<AudioTrack> queue;
    private final AudioPlayer player;
    public AudioTrack lastTrack;
    private boolean repeating = false;
    private GuildMusicManager guildMusicManager;
    private String autoPlaylist;
    private AudioController audioController;
    private TextChannel textChannel;

    TrackScheduler(AudioPlayer player, GuildMusicManager guildMusicManager, AudioController audioController, String autoPlaylist, TextChannel textChannel) {
        this.player = player;
        this.queue = new LinkedList<>();
        this.guildMusicManager = guildMusicManager;
        this.audioController = audioController;
        this.autoPlaylist = autoPlaylist;
        if (textChannel == null) {
            this.textChannel = guildMusicManager.getGuild().getDefaultChannel();
        } else {
            this.textChannel = textChannel;
        }
    }

    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        if (this.queue.poll() == null) {
            this.audioController.loadAndPlay(this.guildMusicManager,
                    this.textChannel,
                    this.autoPlaylist,
                    true);
        }
        AudioTrack audioTrack = queue.poll();
        if (audioTrack == null) {
            if (this.autoPlaylist != null) {
                textChannel.sendMessage("Could not start the auto playlist.").queue();
                Shelfs.getLogger().logMessage("Warning auto playlist is set but no tracks got loaded.", LogLevel.WARNING);
            }
            return;
        }
        player.startTrack(audioTrack, false);
        AudioUtils.sendInfoToDJ(textChannel, audioTrack);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (this.repeating)
                player.startTrack(lastTrack.makeClone(), false);
            else
                this.nextTrack();
        }

    }

    public boolean isRepeating() {
        return this.repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }
}