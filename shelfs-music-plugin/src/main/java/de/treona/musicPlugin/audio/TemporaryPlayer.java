package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Used to check if songs or tracks a valid.
 */
public class TemporaryPlayer {

    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerManager audioPlayerManager;

    /**
     * Creates an instance of a @{@link TrackScheduler} and @{@link AudioPlayer}
     * make sure to destroy() your instance after usage.
     *
     * @param audioPlayerManager used to create a new @{@link AudioPlayer} instance.
     */
    TemporaryPlayer(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
        this.player = audioPlayerManager.createPlayer();
        this.scheduler = new TrackScheduler(player, null, null, null, null, null);
        this.player.addListener(scheduler);
    }

    /**
     * Checks if track(s) are present under a certain identifier.
     *
     * @param identifier place to check for tracks.
     * @return @{@code true} if at least one track was loaded {@code false} if no track(s) could be loaded.
     */
    public boolean isValid(String identifier) {
        Future<Void> future = this.audioPlayerManager.loadItemOrdered(this, identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                tracks.forEach(scheduler::queue);
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
        try {
            future.get(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return scheduler.queue.size() > 0;
    }

    /**
     * Destroys the @{@link AudioPlayer} and the @{@link TrackScheduler}
     * Not calling this could lead to a memory leak.
     */
    public void destroy() {
        this.scheduler.queue.clear();
        this.player.destroy();
    }
}
