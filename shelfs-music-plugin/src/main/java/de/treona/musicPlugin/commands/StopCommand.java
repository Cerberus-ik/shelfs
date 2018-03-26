package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class StopCommand implements GuildCommand {

    private AudioController audioController;

    public StopCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        if (!AudioPermissionUtil.hasAudioPermission(member)) {
            textChannel.sendMessage("Sorry but you don't have the permission to use this command.").queue();
            return;
        }
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(member.getGuild());
        guildMusicManager.scheduler.queue.clear();
        guildMusicManager.player.stopTrack();
        guildMusicManager.player.setPaused(false);
        textChannel.sendMessage("Stopped all playbacks.").queue();
    }

    @Override
    public String getName() {
        return "Stop";
    }

    @Override
    public String getDescription() {
        return "Stops the current track and clears out the queue.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
