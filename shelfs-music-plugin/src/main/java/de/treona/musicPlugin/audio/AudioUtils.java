package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
            stringBuilder.append("``").append(i + 1).append(".) [").append(trackDuration).append("]`` ").append(track.getInfo().title);
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

    static void sendQueuePlaylistInfoToDJ(TextChannel channel, List<AudioTrack> playlist, AudioPlaylist audioPlaylist, int queuePosition, int tracksTooLong) {
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

    public static MessageEmbed buildVolumeMessage(GuildMusicManager guildMusicManager, int newVolume, boolean setValue) {
        AudioPlayer audioPlayer = guildMusicManager.player;
        audioPlayer.setVolume(newVolume);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (setValue) {
            embedBuilder.setTitle("New player volume");
        }
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

    public static String getArguments(String message) {
        String[] args = message.split(" ");
        if (args.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            stringBuilder.append(args[i]);
            if (i - 1 != args.length) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }
}
