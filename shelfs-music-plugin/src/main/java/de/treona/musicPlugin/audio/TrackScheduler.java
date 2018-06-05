package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.treona.musicPlugin.util.AudioMessageUtils;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.logger.LogLevel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    public LinkedHashMap<AudioTrack, Boolean> queue;
    public AudioTrack lastTrack;
    private boolean repeating = false;
    private GuildMusicManager guildMusicManager;
    private String autoPlaylist;
    private AudioController audioController;
    private TextChannel textChannel;
    private Guild guild;

    TrackScheduler(AudioPlayer player, GuildMusicManager guildMusicManager, AudioController audioController, String autoPlaylist, TextChannel textChannel, Guild guild) {
        this.player = player;
        this.queue = new LinkedHashMap<>();
        this.guildMusicManager = guildMusicManager;
        this.audioController = audioController;
        this.autoPlaylist = autoPlaylist;
        this.guild = guild;
        if (textChannel == null && guildMusicManager != null)
            this.textChannel = guildMusicManager.getGuild().getDefaultChannel();
        else
            this.textChannel = textChannel;
    }

    public void queue(AudioTrack track, boolean autoPlaylist) {
        if (!this.player.startTrack(track, true)) {
            this.queue.put(track, autoPlaylist);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        AudioTrack audioTrack = this.getNextTrack();
        if (audioTrack == null && this.autoPlaylist != null && this.autoPlaylist.length() > 2) {
            AudioMessageUtils.sendAutoPlaylistInformation(this.textChannel, "<" + this.autoPlaylist + ">");
            this.audioController.load(this.guildMusicManager,
                    this.textChannel,
                    this.autoPlaylist,
                    true,
                    QueueAction.QUEUE_AND_DO_NOT_PLAY_AUTO_PLAYLIST);
            this.shuffle();
            try {
                audioTrack = this.queue.keySet().iterator().next();
            } catch (NoSuchElementException ignore) {
            }
        }
        if (audioTrack == null) {
            if (this.autoPlaylist != null && this.autoPlaylist.length() > 2) {
                this.textChannel.sendMessage("Could not start the auto playlist.").queue();
                Shelfs.getLogger().logMessage("Warning auto playlist is set but no tracks got loaded.", LogLevel.WARNING);
            }
            return;
        }
        this.queue.remove(audioTrack);
        this.player.startTrack(audioTrack, false);
        AudioMessageUtils.sendPlayInfoToDJ(this.textChannel, audioTrack);
    }

    private AudioTrack getNextTrack() {
        if (this.queue.containsValue(false))
            return this.queue.keySet().stream().filter(track -> !this.queue.get(track)).findFirst().orElse(null);
        else {
            try {
                return this.queue.keySet().iterator().next();
            } catch (NoSuchElementException e) {
                return null;
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (this.repeating)
                player.startTrack(lastTrack.makeClone(), true);
            else
                this.nextTrack();
        } else {
            this.startLeaveRunnable(1000 * 10);
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        this.startLeaveRunnable(1000 * 60);
    }

    public void startLeaveRunnable(int milliSeconds) {
        new Thread(() -> {
            if (!this.guild.getAudioManager().isConnected()) {
                return;
            }
            try {
                sleep(milliSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!this.guild.getAudioManager().isConnected()) {
                return;
            }
            if (this.guild.getAudioManager().getConnectedChannel().getMembers().stream().anyMatch(member -> !member.getUser().isBot())) {
                return;
            }
            if (player.getPlayingTrack() != null) {
                this.player.stopTrack();
                this.queue.clear();
            }
            this.guild.getAudioManager().closeAudioConnection();
        }).start();
    }

    public boolean isRepeating() {
        return this.repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void shuffle() {
        LinkedHashMap<AudioTrack, Boolean> newQueue = new LinkedHashMap<>();
        List<AudioTrack> tracks = new ArrayList<>(this.queue.keySet());
        Collections.shuffle(tracks);
        tracks.forEach(track -> newQueue.put(track, this.queue.get(track)));
        this.queue = newQueue;
    }

    @SuppressWarnings("WeakerAccess")
    public int getPositionOfTrack(AudioTrack track) {
        if (this.queue.get(track) == null)
            return 0;
        else if (this.queue.get(track))
            return new ArrayList<>(new LinkedHashMap<>(this.queue).keySet()).indexOf(track);
        else
            return this.queue.keySet().stream().filter(streamTrack -> !this.queue.get(streamTrack)).collect(Collectors.toList()).indexOf(track);
    }
}