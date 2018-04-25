package de.treona.musicPlugin.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.message.ReactionMessage;
import de.treona.shelfs.api.message.ReactionMessageBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class AudioMessageUtils {

    public static void sendSearchResults(MessageChannel messageChannel, List<AudioTrack> tracks, String identifier) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(66, 89, 244));
        embedBuilder.setTitle("Found: " + tracks.size() + " track(s) ``[" + formatDuration(tracks) + "]``");
        if (tracks.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < Math.min(10, tracks.size()); i++) {
                stringBuilder.append(getTextForSearchItems(tracks.get(i), i + 1));
            }
            embedBuilder.addField("", stringBuilder.toString(), false);
        } else {
            embedBuilder.addField("Found nothing under the given term/link", identifier, false);
        }
        embedBuilder.setFooter("Result of youtube search for: " + identifier, Shelfs.getJda().getSelfUser().getEffectiveAvatarUrl());
        new Thread(() -> {
            Message message = messageChannel.sendMessage(embedBuilder.build()).complete();
            message.addReaction("\u0031\u20E3").queue();
            message.addReaction("\u0032\u20E3").queue();
            message.addReaction("\u0033\u20E3").queue();
            message.addReaction("\u0034\u20E3").queue();
            message.addReaction("\u0035\u20E3").queue();
            message.addReaction("\u0036\u20E3").queue();
            message.addReaction("\u0037\u20E3").queue();
            message.addReaction("\u0038\u20E3").queue();
            message.addReaction("\u0039\u20E3").queue();
            message.addReaction("\uD83D\uDD1F").queue();
        }).start();
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
        List<String> managers = tracks.stream().map(track -> track.getSourceManager().getSourceName()).distinct().map(AudioMessageUtils::getFormattedSourceManagerNameFromTrack).collect(Collectors.toList());
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

    private static String getTextForSearchItems(AudioTrack track, int position) {
        StringBuilder stringBuilder = new StringBuilder();
        /* TODO change numbers to emojis */
        //stringBuilder.append("`` ").append(getEmojiForNumber(position)).append(" ) ``");
        stringBuilder.append("`` ").append(position).append(" ) ``");
        if (track.getInfo().title.length() + track.getInfo().author.length() > 55) {
            stringBuilder.append(track.getInfo().author)
                    .append(" – ").append(track.getInfo().title, 0, 52 - track.getInfo().author.length()).append("...")
                    .append(" ``[")
                    .append(formatDuration(track.getDuration()))
                    .append("]``")
                    .append(System.lineSeparator());
        } else {
            stringBuilder.append(track.getInfo().author)
                    .append(" – ")
                    .append(track.getInfo().title)
                    .append(" ``[")
                    .append(formatDuration(track.getDuration()))
                    .append("]``")
                    .append(System.lineSeparator());
        }
        return stringBuilder.toString();
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

    public static void sendAutoPlaylistInformation(TextChannel channel, String playlistName) {
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

    public static MessageEmbed buildVolumeMessageEmbed(int volume, GuildMusicManager guildMusicManager, ConfigManager configManager) {
        VolumeUtil.setVolume(volume, guildMusicManager, configManager);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.yellow);
        embedBuilder.addField(buildVolumeMessageField(VolumeUtil.MIN_VOLUME, VolumeUtil.MAX_VOLUME, guildMusicManager.player.getVolume()));
        return embedBuilder.build();
    }

    public static ReactionMessage buildVolumeReactionMessage(GuildMusicManager guildMusicManager, ConfigManager configManager, MessageEmbed messageEmbed) {
        ReactionMessageBuilder reactionMessageBuilder = new ReactionMessageBuilder();
        reactionMessageBuilder.setMessage(messageEmbed);
        reactionMessageBuilder.addReaction("\uD83D\uDD09",
                (reaction, user) -> new Thread(() -> {
                    VolumeUtil.quieter(guildMusicManager, configManager);
                    Message message = reaction.getChannel().getMessageById(reaction.getMessageId()).complete();
                    message.editMessage(AudioMessageUtils.buildVolumeMessageEmbed(guildMusicManager.player.getVolume(), guildMusicManager, configManager)).queue();
                }).start());
        reactionMessageBuilder.addReaction("\uD83D\uDD0A",
                (reaction, user) -> new Thread(() -> {
                    VolumeUtil.louder(guildMusicManager, configManager);
                    Message message = reaction.getChannel().getMessageById(reaction.getMessageId()).complete();
                    message.editMessage(AudioMessageUtils.buildVolumeMessageEmbed(guildMusicManager.player.getVolume(), guildMusicManager, configManager)).queue();
                }).start());
        return reactionMessageBuilder.build();
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

    private static String getEmojiForNumber(int number) {
        switch (number) {
            case 1:
                return "\u0031\u20E3";
            case 2:
                return "\u0032\u20E3";
            case 3:
                return "\u0033\u20E3";
            case 4:
                return "\u0034\u20E3";
            case 5:
                return "\u0035\u20E3";
            case 6:
                return "\u0036\u20E3";
            case 7:
                return "\u0037\u20E3";
            case 8:
                return "\u0038\u20E3";
            case 9:
                return "\u0039\u20E3";
            case 10:
                return "\uD83D\uDD1F";
            default:
                return "\u2753";
        }
    }
}
