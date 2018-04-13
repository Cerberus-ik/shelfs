package de.treona.musicPlugin.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.permission.DJPermission;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;

public class SkipCommand implements GuildCommand {

    private AudioController audioController;

    public SkipCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        String[] args = message.getContentRaw().split(" ");
        if (args.length == 1)
            this.skipOne(textChannel, member);
        else
            this.skipMoreThanOneTrack(textChannel, member, args);
    }

    private void skipMoreThanOneTrack(TextChannel textChannel, Member member, String[] args) {
        try {
            int songsToSkip = Integer.parseInt(args[1]);
            if (songsToSkip < 1) {
                textChannel.sendMessage("You have to skip at least one song.").queue();
                return;
            }
            GuildMusicManager musicManager = this.audioController.getMusicManager(member.getGuild());
            songsToSkip = Math.min(songsToSkip, musicManager.scheduler.queue.size() + 1);
            for (int i = 0; i < songsToSkip - 1; i++)
                musicManager.scheduler.queue.poll();
            textChannel.sendMessage("Skipping " + songsToSkip + " songs.").queue();
            musicManager.scheduler.nextTrack();
        } catch (Exception e) {
            textChannel.sendMessage("Please enter a valid number of songs to skip.").queue();
        }
    }

    private void skipOne(TextChannel textChannel, Member member) {
        GuildMusicManager musicManager = this.audioController.getMusicManager(member.getGuild());
        AudioPlayer audioPlayer = musicManager.player;
        AudioTrack track = audioPlayer.getPlayingTrack();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.ORANGE);
        if (track != null) {
            embedBuilder.addField("Skipped:", "" + track.getInfo().title, false);
        } else {
            embedBuilder.addField("Next track...", "", false);
        }
        textChannel.sendMessage(embedBuilder.build()).queue();
        musicManager.scheduler.nextTrack();
    }

    @Override
    public String getName() {
        return "Skip";
    }

    @Override
    public String getDescription() {
        return "Skips the current track and plays the next one.";
    }

    @Override
    public Permission getPermission() {
        return new DJPermission();
    }
}
