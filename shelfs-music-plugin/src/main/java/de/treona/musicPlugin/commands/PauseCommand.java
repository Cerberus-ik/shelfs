package de.treona.musicPlugin.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class PauseCommand implements GuildCommand {

    private AudioController audioController;

    public PauseCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        if (!AudioPermissionUtil.hasAudioPermission(member)) {
            textChannel.sendMessage("Sorry but you don't have the permission to use this command.").queue();
            return;
        }

        AudioPlayer audioPlayer = this.audioController.getMusicManager(member.getGuild()).player;
        if (audioPlayer.getPlayingTrack() == null) {
            textChannel.sendMessage("Nothing is currently playing.").queue();
            return;
        }

        audioPlayer.setPaused(!audioPlayer.isPaused());
        if (audioPlayer.isPaused()) {
            textChannel.sendMessage("The player has been paused.").queue();
        } else {
            textChannel.sendMessage("The player has resumed playing.").queue();
        }
    }

    @Override
    public String getName() {
        return "Pause";
    }

    @Override
    public String getDescription() {
        return "Pauses/continues the current track.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
