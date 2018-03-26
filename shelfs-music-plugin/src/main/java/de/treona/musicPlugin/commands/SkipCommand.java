package de.treona.musicPlugin.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class SkipCommand implements GuildCommand {

    private AudioController audioController;

    public SkipCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        if (!AudioPermissionUtil.hasAudioPermission(member)) {
            textChannel.sendMessage("Sorry but you don't have the permission to use this command.").queue();
            return;
        }
        AudioPlayer audioPlayer = this.audioController.getMusicManager(member.getGuild()).player;
        AudioTrack track = audioPlayer.getPlayingTrack();
        if (track != null) {
            textChannel.sendMessage("Skipped: " + track.getInfo().title).queue();
        } else {
            textChannel.sendMessage("Next song...").queue();
        }
        this.audioController.getMusicManager(member.getGuild()).scheduler.nextTrack();

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
        return null;
    }
}
