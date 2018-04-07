package de.treona.musicPlugin.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.AudioUtils;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;

public class NowPlayingCommand implements GuildCommand {

    private AudioController audioController;

    public NowPlayingCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    public static void addReactions(Message message, AudioController audioController) {
        if (audioController.getMusicManager(message.getGuild()).player.isPaused())
            message.addReaction("▶").complete();
        else
            message.addReaction("\u23F8").complete();
        if (audioController.getMusicManager(message.getGuild()).scheduler.isRepeating())
            message.addReaction("\uD83D\uDD02").complete();
        else
            message.addReaction("\uD83D\uDD01").complete();
        message.addReaction("⏩").complete();
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(member.getGuild());
        AudioPlayer audioPlayer = guildMusicManager.player;
        AudioTrack currentTrack = audioPlayer.getPlayingTrack();
        if (currentTrack == null || !currentTrack.getInfo().isStream) {
            this.sendMessage(textChannel, this.generateTrackMessage(currentTrack));
        } else {
            this.sendMessage(textChannel, this.generateStreamMessage(currentTrack));
        }
    }

    private MessageEmbed generateStreamMessage(AudioTrack audioTrack) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Stream: " + audioTrack.getInfo().title);
        embedBuilder.setColor(new Color(152, 172, 80));
        embedBuilder.addField(this.generateStreamField(audioTrack));
        embedBuilder.addField("", audioTrack.getInfo().uri, false);
        return embedBuilder.build();
    }

    private MessageEmbed generateTrackMessage(AudioTrack audioTrack) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (audioTrack != null) {
            embedBuilder.setTitle("**Playing from:** " +
                    audioTrack.getSourceManager().getSourceName()
                            .replaceFirst(audioTrack.getSourceManager().getSourceName().substring(0, 1),
                                    audioTrack.getSourceManager().getSourceName()
                                            .substring(0, 1).toUpperCase()));
        }
        embedBuilder.addField(this.generateTrackField(audioTrack));
        if (audioTrack != null) {
            embedBuilder.setDescription(audioTrack.getInfo().uri);
        }
        embedBuilder.setColor(new Color(152, 172, 80));
        return embedBuilder.build();
    }

    private MessageEmbed.Field generateStreamField(AudioTrack audioTrack) {
        if (audioTrack == null) {
            return new MessageEmbed.Field("Nothing", "Nothing is playing right now.", false);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("▶");
        for (int i = 0; i < 10; i++) {
            stringBuilder.append("➖");
        }
        stringBuilder.append("⭕");
        stringBuilder.append(String.format("``[%s-%s]``",
                AudioUtils.formatDuration(audioTrack.getPosition()),
                "∞"));
        return new MessageEmbed.Field(audioTrack.getInfo().title, stringBuilder.toString(), false);
    }

    private MessageEmbed.Field generateTrackField(AudioTrack audioTrack) {
        if (audioTrack == null) {
            return new MessageEmbed.Field("Nothing", "Nothing is playing right now.", false);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("▶");
        double position = audioTrack.getPosition();
        double length = audioTrack.getDuration();
        int part = (int) Math.round(position / length * 10);
        for (int i = 0; i < part; i++) {
            stringBuilder.append("▬");
        }
        stringBuilder.append("⭕**");
        for (int i = part; i < 10; i++) {
            stringBuilder.append("▬");
        }
        stringBuilder.append("**");
        stringBuilder.append(String.format("``[%s-%s]``",
                AudioUtils.formatDuration(audioTrack.getPosition()),
                AudioUtils.formatDuration(audioTrack.getDuration())));
        return new MessageEmbed.Field(audioTrack.getInfo().title, stringBuilder.toString(), false);
    }

    @Override
    public String getName() {
        return "Now playing";
    }

    @Override
    public String getDescription() {
        return "Shows you the currently playing song.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }

    private void sendMessage(TextChannel textChannel, MessageEmbed messageEmbed) {
        new Thread(() -> {
            Message message = textChannel.sendMessage(messageEmbed).complete();
            if (messageEmbed.getTitle() != null) {
                addReactions(message, audioController);
            }
        }).start();
    }
}
