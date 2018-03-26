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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AudioController extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<String, GuildMusicManager> musicManagers;
    private ConfigManager configManager;

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

    //Prefix for all commands: .
    //Example:  .play
    //Current commands
    // join [name]  - Joins a voice channel that has the provided name
    // join [id]    - Joins a voice channel based on the provided id.
    // leave        - Leaves the voice channel that the bot is currently in.
    // play         - Plays songs from the current queue. Starts playing again if it was previously paused
    // play [url]   - Adds a new song to the queue and starts playing if it wasn't playing already
    // pplay        - Adds a playlist to the queue and starts playing if not already playing
    // pause        - Pauses audio playback
    // stop         - Completely stops audio playback, skipping the current song.
    // skip         - Skips the current song, automatically starting the next
    // nowplaying   - Prints information about the currently playing song (title, current time)
    // np           - alias for nowplaying
    // list         - Lists the songs in the queue
    // volume [val] - Sets the volume of the MusicPlayer [10 - 100]
    // restart      - Restarts the current song or restarts the previous song if there is no current song playing.
    // repeat       - Makes the player repeat the currently playing song
    // reset        - Completely resets the player, fixing all errors and clearing the queue.
/*  @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT))
            return;

        try {
            List<String> allowedIds = Files.readAllLines(Paths.get("admins.txt"));
            if (!allowedIds.contains(event.getAuthor().getId()))
                return;
        } catch (IOException ignored) {
            //If we encounter an ioe, it is due to the file not existing.
            //In that case, we treat the music system as not having admin restrictions.
        }

        String[] command = event.getMessage().getContentDisplay().split(" ", 2);
        if (!command[0].startsWith("."))    //message doesn't start with prefix.
            return;

        Guild guild = event.getGuild();
        GuildMusicManager mng = getMusicManager(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;

        if (".join".equals(command[0])) {
            if (command.length == 1) //No channel name was provided to search for.
            {
                event.getChannel().sendMessage("No channel name was provided to search with to join.").queue();
            } else {
                VoiceChannel chan = null;
                try {
                    chan = guild.getVoiceChannelById(command[1]);
                } catch (NumberFormatException ignored) {
                }

                if (chan == null)
                    chan = guild.getVoiceChannelsByName(command[1], true).stream().findFirst().orElse(null);
                if (chan == null) {
                    event.getChannel().sendMessage("Could not find VoiceChannel by name: " + command[1]).queue();
                } else {
                    guild.getAudioManager().setSendingHandler(mng.sendHandler);

                    try {
                        guild.getAudioManager().openAudioConnection(chan);
                    } catch (PermissionException e) {
                        if (e.getPermission() == Permission.VOICE_CONNECT) {
                            event.getChannel().sendMessage("Yui does not have permission to connect to: " + chan.getName()).queue();
                        }
                    }
                }
            }
        } else if (".leave".equals(command[0])) {
            guild.getAudioManager().setSendingHandler(null);
            guild.getAudioManager().closeAudioConnection();
        } else if (".play".equals(command[0])) {
            if (command.length == 1) //It is only the command to start playback (probably after pause)
            {
                if (player.isPaused()) {
                    player.setPaused(false);
                    event.getChannel().sendMessage("Playback as been resumed.").queue();
                } else if (player.getPlayingTrack() != null) {
                    event.getChannel().sendMessage("Player is already playing!").queue();
                } else if (scheduler.queue.isEmpty()) {
                    event.getChannel().sendMessage("The current audio queue is empty! Add something to the queue first!").queue();
                }
            } else    //Commands has 2 parts, .play and url.
            {
                //loadAndPlay(mng, event.getChannel(), command[1], false);
            }
        } else if (".pplay".equals(command[0]) && command.length == 2) {
            //loadAndPlay(mng, event.getChannel(), command[1], true);
        } else if (".skip".equals(command[0])) {
            scheduler.nextTrack();
            event.getChannel().sendMessage("The current track was skipped.").queue();
        } else if (".pause".equals(command[0])) {
            if (player.getPlayingTrack() == null) {
                event.getChannel().sendMessage("Cannot pause or resume player because no track is loaded for playing.").queue();
                return;
            }

            player.setPaused(!player.isPaused());
            if (player.isPaused())
                event.getChannel().sendMessage("The player has been paused.").queue();
            else
                event.getChannel().sendMessage("The player has resumed playing.").queue();
        } else if (".stop".equals(command[0])) {
            scheduler.queue.clear();
            player.stopTrack();
            player.setPaused(false);
            event.getChannel().sendMessage("Playback has been completely stopped and the queue has been cleared.").queue();
        } else if (".volume".equals(command[0])) {
            if (command.length == 1) {
                event.getChannel().sendMessage("Current player volume: **" + player.getVolume() + "**").queue();
            } else {
                try {
                    int newVolume = Math.max(10, Math.min(100, Integer.parseInt(command[1])));
                    int oldVolume = player.getVolume();
                    player.setVolume(newVolume);
                    event.getChannel().sendMessage("Player volume changed from `" + oldVolume + "` to `" + newVolume + "`").queue();
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("`" + command[1] + "` is not a valid integer. (10 - 100)").queue();
                }
            }
        } else if (".restart".equals(command[0])) {
            AudioTrack track = player.getPlayingTrack();
            if (track == null)
                track = scheduler.lastTrack;

            if (track != null) {
                event.getChannel().sendMessage("Restarting track: " + track.getInfo().title).queue();
                player.playTrack(track.makeClone());
            } else {
                event.getChannel().sendMessage("No track has been previously started, so the player cannot replay a track!").queue();
            }
        } else if (".repeat".equals(command[0])) {
            scheduler.setRepeating(!scheduler.isRepeating());
            event.getChannel().sendMessage("Player was set to: **" + (scheduler.isRepeating() ? "repeat" : "not repeat") + "**").queue();
        } else if (".reset".equals(command[0])) {
            synchronized (musicManagers) {
                scheduler.queue.clear();
                player.destroy();
                guild.getAudioManager().setSendingHandler(null);
                musicManagers.remove(guild.getId());
            }

            mng = getMusicManager(guild);
            guild.getAudioManager().setSendingHandler(mng.sendHandler);
            event.getChannel().sendMessage("The player has been completely reset!").queue();

        } else if (".nowplaying".equals(command[0]) || ".np".equals(command[0])) {
            AudioTrack currentTrack = player.getPlayingTrack();
            if (currentTrack != null) {
                String title = currentTrack.getInfo().title;
                String position = AudioUtils.formatDuration(currentTrack.getPosition());
                String duration = AudioUtils.formatDuration(currentTrack.getDuration());

                String nowplaying = String.format("**Playing:** %s\n**Time:** [%s / %s]",
                        title, position, duration);

                event.getChannel().sendMessage(nowplaying).queue();
            } else
                event.getChannel().sendMessage("The player is not currently playing anything!").queue();
        } else if (".list".equals(command[0])) {
            Queue<AudioTrack> queue = scheduler.queue;
            synchronized (queue) {
                if (queue.isEmpty()) {
                    event.getChannel().sendMessage("The queue is currently empty!").queue();
                } else {
                    int trackCount = 0;
                    long queueLength = 0;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Current Queue: Entries: ").append(queue.size()).append("\n");
                    for (AudioTrack track : queue) {
                        queueLength += track.getDuration();
                        if (trackCount < 10) {
                            sb.append("`[").append(AudioUtils.formatDuration(track.getDuration())).append("]` ");
                            sb.append(track.getInfo().title).append("\n");
                            trackCount++;
                        }
                    }
                    sb.append("\n").append("Total Queue Time Length: ").append(AudioUtils.formatDuration(queueLength));

                    event.getChannel().sendMessage(sb.toString()).queue();
                }
            }
        } else if (".shuffle".equals(command[0])) {
            if (scheduler.queue.isEmpty()) {
                event.getChannel().sendMessage("The queue is currently empty!").queue();
                return;
            }
            scheduler.shuffle();
            event.getChannel().sendMessage("The queue has been shuffled!").queue();
        }
    }*/

    public void loadAndPlay(GuildMusicManager guildMusicManager, final TextChannel channel, String url, boolean blocking) {
        Future<Void> future = playerManager.loadItemOrdered(guildMusicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (isTooLong(guildMusicManager.getGuild(), track)) {
                    AudioUtils.sendInfoTrackTooLong(channel, track);
                    return;
                }
                guildMusicManager.scheduler.queue(track);
                AudioUtils.sendInfoToDJ(channel, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                List<AudioTrack> filteredTracks = tracks.stream().filter(track -> !isTooLong(guildMusicManager.getGuild(), track)).collect(Collectors.toList());
                if (filteredTracks.size() == tracks.size()) {
                    channel.sendMessage("Adding **" + playlist.getTracks().size() + "** tracks to queue from playlist: " + playlist.getName()).queue();
                    tracks.forEach(guildMusicManager.scheduler::queue);
                    AudioTrack lastTrack = guildMusicManager.player.getPlayingTrack();
                    if (lastTrack != null && lastTrack.equals(playlist.getTracks().get(0))) {
                        AudioUtils.sendInfoToDJ(channel, playlist.getTracks().get(0));
                    }
                } else if (filteredTracks.size() == 0) {
                    channel.sendMessage("All tracks are too long (" + tracks.size() + ")").queue();
                    return;
                }
                channel.sendMessage("Adding **" + playlist.getTracks().size() + "** tracks to queue (" + (tracks.size() - filteredTracks.size()) + " where too long) from playlist: " + playlist.getName()).queue();
                filteredTracks.forEach(guildMusicManager.scheduler::queue);
                AudioTrack lastTrack = guildMusicManager.player.getPlayingTrack();
                if (lastTrack != null && lastTrack.equals(filteredTracks.get(0))) {
                    AudioUtils.sendInfoToDJ(channel, filteredTracks.get(0));
                }
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
                future.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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