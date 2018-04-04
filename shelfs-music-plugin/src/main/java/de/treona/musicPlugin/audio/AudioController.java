package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.musicPlugin.config.ConfigManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class AudioController extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<String, GuildMusicManager> musicManagers;
    private ConfigManager configManager;
    private final int TIMEOUT = 25000;

    public AudioController(ConfigManager configManager) {
        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());

        musicManagers = new HashMap<>();
        this.configManager = configManager;
    }

    public void loadAndPlayNow(GuildMusicManager guildMusicManager, final TextChannel channel, String url, boolean blocking) {
        boolean isSearch = !Objects.equals(this.formatUrl(url), url);
        Future<Void> future = playerManager.loadItemOrdered(guildMusicManager, this.formatUrl(url), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (isTooLong(guildMusicManager.getGuild(), track)) {
                    AudioUtils.sendInfoTrackTooLong(channel, track);
                    return;
                }
                List<AudioTrack> queue = new ArrayList<>(guildMusicManager.scheduler.queue);
                guildMusicManager.scheduler.queue.clear();
                guildMusicManager.player.stopTrack();
                guildMusicManager.scheduler.queue(track);
                AudioUtils.sendPlayInfoToDJ(channel, track);
                queue.forEach(guildMusicManager.scheduler::queue);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                List<AudioTrack> filteredTracks = tracks.stream().filter(track -> !isTooLong(guildMusicManager.getGuild(), track)).collect(Collectors.toList());
                if (filteredTracks.size() == 0) {
                    channel.sendMessage("All tracks are too long (" + tracks.size() + ")").queue();
                    return;
                }
                if (isSearch) {
                    this.trackLoaded(filteredTracks.get(0));
                    return;
                }
                if (filteredTracks.size() == tracks.size()) {
                    List<AudioTrack> queue = new ArrayList<>(guildMusicManager.scheduler.queue);
                    guildMusicManager.scheduler.queue.clear();
                    guildMusicManager.player.stopTrack();
                    tracks.forEach(guildMusicManager.scheduler::queue);
                    queue.forEach(guildMusicManager.scheduler::queue);
                    guildMusicManager.scheduler.nextTrack();
                    AudioUtils.sendQueuePlaylistInfoToDJ(channel, playlist.getTracks(), playlist, 0, 0);
                    return;
                }
                Queue<AudioTrack> queue = guildMusicManager.scheduler.queue;
                guildMusicManager.scheduler.queue.clear();
                guildMusicManager.player.stopTrack();
                filteredTracks.forEach(guildMusicManager.scheduler::queue);
                queue.forEach(guildMusicManager.scheduler::queue);
                guildMusicManager.scheduler.nextTrack();
                AudioUtils.sendQueuePlaylistInfoToDJ(channel, playlist.getTracks(), playlist, 0, filteredTracks.size());
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Sorry but I couldn't find something to play there.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
        if (blocking) {
            try {
                future.get(this.TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadAndPlayNext(GuildMusicManager guildMusicManager, final TextChannel channel, String url, boolean blocking) {
        boolean isSearch = !Objects.equals(this.formatUrl(url), url);
        Future<Void> future = playerManager.loadItemOrdered(guildMusicManager, this.formatUrl(url), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (isTooLong(guildMusicManager.getGuild(), track)) {
                    AudioUtils.sendInfoTrackTooLong(channel, track);
                    return;
                }
                List<AudioTrack> queue = new ArrayList<>(guildMusicManager.scheduler.queue);
                guildMusicManager.scheduler.queue.clear();
                guildMusicManager.scheduler.queue(track);
                queue.forEach(guildMusicManager.scheduler::queue);
                AudioUtils.sendQueueInfoToDJ(channel, track, 1);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                List<AudioTrack> filteredTracks = tracks.stream().filter(track -> !isTooLong(guildMusicManager.getGuild(), track)).collect(Collectors.toList());
                if (filteredTracks.size() == 0) {
                    channel.sendMessage("All tracks are too long (" + tracks.size() + ")").queue();
                    return;
                }
                if (isSearch) {
                    this.trackLoaded(filteredTracks.get(0));
                    return;
                }
                if (filteredTracks.size() == tracks.size()) {
                    Queue<AudioTrack> queue = guildMusicManager.scheduler.queue;
                    guildMusicManager.scheduler.queue.clear();
                    tracks.forEach(guildMusicManager.scheduler::queue);
                    queue.forEach(guildMusicManager.scheduler::queue);
                    AudioUtils.sendQueuePlaylistInfoToDJ(channel, playlist.getTracks(), playlist, 0, 0);
                    return;
                }
                List<AudioTrack> queue = new ArrayList<>(guildMusicManager.scheduler.queue);
                guildMusicManager.scheduler.queue.clear();
                filteredTracks.forEach(guildMusicManager.scheduler::queue);
                queue.forEach(guildMusicManager.scheduler::queue);
                AudioUtils.sendQueuePlaylistInfoToDJ(channel, playlist.getTracks(), playlist, 0, filteredTracks.size());
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Sorry but I couldn't find something to play there.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
        if (blocking) {
            try {
                future.get(this.TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadAndPlay(GuildMusicManager guildMusicManager, final TextChannel channel, String url, boolean blocking) {
        boolean isSearch = !Objects.equals(this.formatUrl(url), url);
        Future<Void> future = playerManager.loadItemOrdered(guildMusicManager, this.formatUrl(url), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (isTooLong(guildMusicManager.getGuild(), track)) {
                    AudioUtils.sendInfoTrackTooLong(channel, track);
                    return;
                }
                guildMusicManager.scheduler.queue(track);
                if (guildMusicManager.scheduler.queue.size() <= 1) {
                    AudioUtils.sendPlayInfoToDJ(channel, track);
                } else {
                    AudioUtils.sendQueueInfoToDJ(channel, track, guildMusicManager.scheduler.queue.size());
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                List<AudioTrack> filteredTracks = tracks.stream().filter(track -> !isTooLong(guildMusicManager.getGuild(), track)).collect(Collectors.toList());
                int queuePosition = guildMusicManager.scheduler.queue.size() + 1;
                if (isSearch && !filteredTracks.isEmpty()) {
                    this.trackLoaded(filteredTracks.get(0));
                    return;
                }
                if (filteredTracks.size() == tracks.size()) {
                    tracks.forEach(guildMusicManager.scheduler::queue);
                    AudioUtils.sendQueuePlaylistInfoToDJ(channel, playlist.getTracks(), playlist, queuePosition, 0);
                    return;
                } else if (filteredTracks.size() == 0) {
                    channel.sendMessage("All tracks are too long (" + tracks.size() + ")").queue();
                    return;
                }
                filteredTracks.forEach(guildMusicManager.scheduler::queue);
                AudioUtils.sendQueuePlaylistInfoToDJ(channel, filteredTracks, playlist, queuePosition, filteredTracks.size());
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Sorry but I couldn't find something to play there.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
        if (blocking) {
            try {
                future.get(this.TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException ignore) {
            return "ytsearch: " + url;
        }
        return url;
    }

    private boolean isTooLong(Guild guild, AudioTrack audioTrack) {
        if (audioTrack.getInfo().isStream) {
            return false;
        }
        return this.configManager.getGuildSettings(guild).getMaxSongLength() < TimeUnit.MILLISECONDS.toSeconds(audioTrack.getDuration());
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        String guildId = guild.getId();
        GuildMusicManager guildMusicManager = musicManagers.get(guildId);
        if (guildMusicManager == null) {
            synchronized (musicManagers) {
                guildMusicManager = musicManagers.get(guildId);
                if (guildMusicManager == null) {
                    guildMusicManager = new GuildMusicManager(playerManager, guild, this.configManager.getGuildSettings(guild), this);
                    guildMusicManager.player.setVolume(this.getVolume(guild));
                    musicManagers.put(guildId, guildMusicManager);
                    guild.getAudioManager().setSendingHandler(guildMusicManager.sendHandler);
                }
            }
        }
        return guildMusicManager;
    }

    private int getVolume(Guild guild) {
        return this.configManager.getGuildSettings(guild).getVolume();
    }

    public TemporaryPlayer generateTemporaryPlayer() {
        return new TemporaryPlayer(this.playerManager);
    }

}