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
import de.treona.musicPlugin.config.GuildSettings;
import de.treona.musicPlugin.util.AudioMessageUtils;
import de.treona.musicPlugin.util.SearchManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class AudioController {

    private final AudioPlayerManager playerManager;
    private final Map<String, GuildMusicManager> musicManagers;
    private final SearchManager searchManager;
    private ConfigManager configManager;
    @SuppressWarnings("FieldCanBeLocal")
    private final int TIMEOUT = 25000;

    public AudioController(ConfigManager configManager) {
        this.playerManager = new DefaultAudioPlayerManager();
        this.searchManager = new SearchManager();
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

    public void load(GuildMusicManager guildMusicManager, final TextChannel channel, String identifier, boolean blocking, QueueAction action) {
        boolean isSearch = !Objects.equals(this.formatUrl(identifier), identifier);
        Future<Void> future = playerManager.loadItemOrdered(guildMusicManager, this.formatUrl(identifier), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                List<AudioTrack> trackList = new ArrayList<>();
                trackList.add(track);
                this.handleTracks(trackList);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                this.handleTracks(playlist.getTracks());
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Sorry but I couldn't find something to play there.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }

            private void handleTracks(List<AudioTrack> tracks) {
                List<AudioTrack> filteredTracks = tracks.stream().filter(track -> !isTooLong(guildMusicManager.getGuild(), track)).collect(Collectors.toList());
                if (filteredTracks.size() == 0) {
                    channel.sendMessage("All tracks are too long (" + tracks.size() + ")").queue();
                    return;
                }
                if (isSearch) {
                    searchManager.addSearch(channel.getGuild(), tracks);
                    AudioMessageUtils.sendSearchResults(channel, tracks, identifier);
                    return;
                }
                LinkedHashMap<AudioTrack, Boolean> queue = guildMusicManager.scheduler.queue;
                queue = this.addTracks(queue, filteredTracks, action);
                guildMusicManager.scheduler.queue = queue;
                AudioMessageUtils.sendQueueInfo(channel, filteredTracks, guildMusicManager);
                if (action.equals(QueueAction.PLAY_NOW) && guildMusicManager.scheduler.queue.size() > 0) {
                    guildMusicManager.scheduler.nextTrack();
                }
                if (guildMusicManager.player.getPlayingTrack() == null &&
                        (!action.equals(QueueAction.QUEUE_AND_DO_NOT_PLAY)
                                && !action.equals(QueueAction.QUEUE_AND_DO_NOT_PLAY_AUTO_PLAYLIST))) {
                    guildMusicManager.scheduler.nextTrack();
                }
            }

            private LinkedHashMap<AudioTrack, Boolean> addTracks(LinkedHashMap<AudioTrack, Boolean> queue, List<AudioTrack> tracks, QueueAction action) {
                if (action.equals(QueueAction.QUEUE_AND_DO_NOT_PLAY) || action.equals(QueueAction.QUEUE_AND_PLAY)) {
                    tracks.forEach(track -> queue.put(track, false));
                } else if (action.equals(QueueAction.QUEUE_AND_DO_NOT_PLAY_AUTO_PLAYLIST)) {
                    tracks.forEach(track -> queue.put(track, true));
                } else if (action.equals(QueueAction.PLAY_NEXT) || action.equals(QueueAction.PLAY_NOW)) {
                    Queue<AudioTrack> tempQueue = new LinkedList<>(queue.keySet());
                    queue.clear();
                    tracks.forEach(track -> queue.put(track, false));
                    tempQueue.forEach(track -> queue.put(track, false));
                }
                return queue;
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
        GuildSettings guildSettings = this.configManager.getGuildSettings(guild);
        if (guildSettings.maxSongLength == 0) {
            return false;
        }
        return guildSettings.maxSongLength < TimeUnit.MILLISECONDS.toSeconds(audioTrack.getDuration());
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
        return this.configManager.getGuildSettings(guild).volume;
    }

    public SearchManager getSearchManager() {
        return searchManager;
    }

    public TemporaryPlayer generateTemporaryPlayer() {
        return new TemporaryPlayer(this.playerManager);
    }

}