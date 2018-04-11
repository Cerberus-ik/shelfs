package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.shelfs.api.Shelfs;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AudioUtils {

    public static boolean joinMember(Member member) {
        if (!member.getVoiceState().inVoiceChannel()) {
            return false;
        }
        connect(member.getVoiceState().getChannel());
        return true;
    }

    public static void connect(VoiceChannel voiceChannel) {
        if (voiceChannel == null) {
            return;
        }
        VoiceChannel currentChannel = voiceChannel.getGuild().getAudioManager().getConnectedChannel();
        if (!voiceChannel.equals(currentChannel)) {
            voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void sendQueueInfo(MessageChannel messageChannel, List<AudioTrack> tracks, GuildMusicManager guildMusicManager) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(66, 89, 244));
        embedBuilder.setTitle("Queued: " + tracks.size() + " track(s) ``[" + formatDuration(tracks) + "]``");
        if (tracks.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < Math.min(10, tracks.size()); i++) {
                stringBuilder.append(getTextForTrackQueueInfo(guildMusicManager, tracks.get(i)));
            }
            embedBuilder.addField("", stringBuilder.toString(), false);
        } else {
            AudioTrack track = tracks.get(0);
            embedBuilder.addField(getTextForTrackQueueInfo(guildMusicManager, track),
                    "Playing from: " + getFormattedSourceManagerNameFromTrack(track), false);
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<String> managers = tracks.stream().map(track -> track.getSourceManager().getSourceName()).distinct().map(AudioUtils::getFormattedSourceManagerNameFromTrack).collect(Collectors.toList());
        if (managers.size() == 1) {
            stringBuilder.append("Playing from: ").append(managers.get(0));
        } else if (managers.size() > 1) {
            stringBuilder.append("Playing from: ");
            for (int i = 0; i < Math.min(3, managers.size()); i++) {
                if (i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(managers.get(i));
                if (i == Math.min(3, managers.size()) && i < managers.size()) {
                    stringBuilder.append("...");
                }
            }
        }
        //TODO add user who queued here
        embedBuilder.setFooter(stringBuilder.toString(), Shelfs.getJda().getSelfUser().getEffectiveAvatarUrl());
        messageChannel.sendMessage(embedBuilder.build()).queue();
    }

    private static String getTextForTrackQueueInfo(GuildMusicManager guildMusicManager, AudioTrack track) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("``");
        int position = guildMusicManager.scheduler.getPositionOfTrack(track) + 1;
        if (position == 1)
            stringBuilder.append("Next song:").append("``").append("  ");
        else
            stringBuilder.append(position).append(") ``");
        if (track.getInfo().title.length() + track.getInfo().author.length() > 55) {
            stringBuilder.append(track.getInfo().author)
                    .append(" – ").append(track.getInfo().title, 0, 52 - track.getInfo().author.length()).append("...")
                    .append(" ``[")
                    .append(formatDuration(track.getDuration()))
                    .append("]``").append(System.lineSeparator());
        } else {
            stringBuilder.append(track.getInfo().author)
                    .append(" – ")
                    .append(track.getInfo().title)
                    .append(" ``[")
                    .append(formatDuration(track.getDuration()))
                    .append("]``").append(System.lineSeparator());
        }
        if (position == 0)
            stringBuilder.append(System.lineSeparator());
        return stringBuilder.toString();
    }

    public static MessageEmbed buildQueueMessage(List<AudioTrack> tracks, int site, MessageChannel channel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        long duration = 0;
        for (AudioTrack track : tracks) {
            if (track.getInfo().isStream) {
                continue;
            }
            duration = duration + track.getDuration();
        }
        embedBuilder.setTitle("Queue: " + tracks.size() + " ``[" + formatDuration(duration) + "]``");
        embedBuilder.setColor(new Color(0, 255, 233));
        embedBuilder.setFooter("Site: " + site + "/" + (((int) Math.ceil(tracks.size() / 10)) + 1), channel.getJDA().getSelfUser().getEffectiveAvatarUrl());
        int start = 10 * site - 10;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = start; i < Math.min(tracks.size(), start + 10); i++) {
            AudioTrack track = tracks.get(i);
            String trackDuration = "∞";
            if (!tracks.get(i).getInfo().isStream) {
                trackDuration = formatDuration(tracks.get(i).getDuration());
            }
            if (track.getInfo().title.length() > 63) {
                stringBuilder.append("``").append(i + 1).append(".) [").append(trackDuration).append("]`` ").append(track.getInfo().title, 0, 60).append("...");
            } else {
                stringBuilder.append("``").append(i + 1).append(".) [").append(trackDuration).append("]`` ").append(track.getInfo().title);
            }
            stringBuilder.append(System.lineSeparator());
        }
        embedBuilder.addField("", stringBuilder.toString(), false);
        return embedBuilder.build();
    }

    static void sendAutoPlaylistInformation(TextChannel channel, String playlistName) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setTitle("No songs in the queue left.");
        embedBuilder.addField("Playing songs from the auto playlist", "Playing from: " + playlistName, false);
        channel.sendMessage(embedBuilder.build()).queue();
    }

    public static void sendPlayInfoToDJ(TextChannel channel, AudioTrack track) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.MAGENTA);
        char firstCharacter = track.getSourceManager().getSourceName().charAt(0);
        if (track.getInfo().isStream) {
            embedBuilder.setTitle("Streaming from: "
                    + track.getSourceManager().getSourceName()
                    .replaceFirst(String.valueOf(firstCharacter), String.valueOf(firstCharacter).toUpperCase()));
            embedBuilder.addField(track.getInfo().title, "From: " + track.getInfo().author + System.lineSeparator()
                    + "``[∞]``", false);
            channel.sendMessage(embedBuilder.build()).queue();
        } else {
            embedBuilder.setTitle("Playing music from: "
                    + track.getSourceManager().getSourceName()
                    .replaceFirst(String.valueOf(firstCharacter), String.valueOf(firstCharacter).toUpperCase()));
            embedBuilder.addField(track.getInfo().title,
                    "From: " + track.getInfo().author
                            + "``[" + formatDuration(track.getDuration()) + "]``", false);
            channel.sendMessage(embedBuilder.build()).queue();
        }
    }

    static void sendQueueInfoToDJ(TextChannel channel, AudioTrack track, int queuePosition) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.MAGENTA);
        char firstCharacter = track.getSourceManager().getSourceName().charAt(0);
        if (track.getInfo().isStream) {
            embedBuilder.setTitle("Streaming from: "
                    + track.getSourceManager().getSourceName()
                    .replaceFirst(String.valueOf(firstCharacter), String.valueOf(firstCharacter).toUpperCase()));
            embedBuilder.addField(track.getInfo().title, "From: " + track.getInfo().author + System.lineSeparator()
                    + "Queue position: ``" + queuePosition + "``", false);
            channel.sendMessage(embedBuilder.build()).queue();
        } else {
            embedBuilder.setTitle("Music from: "
                    + track.getSourceManager().getSourceName()
                    .replaceFirst(String.valueOf(firstCharacter), String.valueOf(firstCharacter).toUpperCase()));
            embedBuilder.addField(track.getInfo().title + " ``[" + formatDuration(track.getDuration()) + "]``", "From: " + track.getInfo().author + System.lineSeparator()
                    + "Queue position: ``" + queuePosition + "``", false);
            channel.sendMessage(embedBuilder.build()).queue();
        }
    }

    static void sendQueuePlaylistInfoToDJ(TextChannel channel, List<AudioTrack> playlist, AudioPlaylist
            audioPlaylist, int queuePosition, int tracksTooLong) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.MAGENTA);
        embedBuilder.setTitle("Playlist: " + audioPlaylist.getName());
        long duration = 0;
        for (AudioTrack audioTrack : playlist) {
            duration = duration + audioTrack.getDuration();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < Math.min(playlist.size(), 4); i++) {
            stringBuilder.append(System.lineSeparator())
                    .append("``")
                    .append(i + queuePosition)
                    .append(")`` ")
                    .append(playlist.get(i).getInfo().title)
                    .append("``[")
                    .append(formatDuration(playlist.get(i).getDuration()))
                    .append("]``");
        }
        embedBuilder.addField("Tracks: " + playlist.size() + " ``[" + formatDuration(duration) + "]``",
                stringBuilder.toString(), false);
        channel.sendMessage(embedBuilder.build()).queue();
    }

    static void sendInfoTrackTooLong(TextChannel channel, AudioTrack track) {
        channel.sendMessage("The track:" + track.getInfo().title + " is too long (" + TimeUnit.MILLISECONDS.toSeconds(track.getDuration()) + ")").queue();
    }

    public static MessageEmbed buildVolumeMessage(GuildMusicManager guildMusicManager, int newVolume,
                                                  boolean setValue) {
        AudioPlayer audioPlayer = guildMusicManager.player;
        audioPlayer.setVolume(newVolume);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.yellow);
        embedBuilder.addField(buildVolumeMessageField(3, 125, newVolume));
        return embedBuilder.build();
    }

    @SuppressWarnings("SameParameterValue")
    private static MessageEmbed.Field buildVolumeMessageField(double minVolume, double maxVolume, double volume) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\uD83D\uDCA0");
        double range = maxVolume - minVolume;
        double newVolume = volume - minVolume;
        double value = newVolume / range * 10;
        for (int i = 0; i < Math.round(value); i++) {
            stringBuilder.append("▬");
        }
        stringBuilder.append("\uD83D\uDD0A");
        for (int i = (int) Math.round(value); i < 10; i++) {
            stringBuilder.append("▬");
        }
        stringBuilder.append(" ``[").append((int) volume).append("]``");
        return new MessageEmbed.Field("Player volume", stringBuilder.toString(), false);
    }

    public static String formatDuration(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    @SuppressWarnings("WeakerAccess")
    public static String formatDuration(List<AudioTrack> tracks) {
        long duration = 0;
        for (AudioTrack track : tracks) {
            duration = duration + track.getDuration();
        }
        return formatDuration(duration);
    }

    private static String getFormattedSourceManagerNameFromTrack(AudioTrack track) {
        String sourceName = track.getSourceManager().getSourceName();
        return sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1, sourceName.length());
    }

    private static String getFormattedSourceManagerNameFromTrack(String sourceName) {
        return sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1, sourceName.length());
    }
}
