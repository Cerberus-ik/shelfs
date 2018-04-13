package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.permission.DJPermission;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class RepeatCommand implements GuildCommand {

    private AudioController audioController;

    public RepeatCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(member.getGuild());
        boolean isRepeating = guildMusicManager.scheduler.isRepeating();
        guildMusicManager.scheduler.setRepeating(!isRepeating);
        if (isRepeating) {
            textChannel.sendMessage("Repeat mode is now deactivated!").queue();
        } else {
            textChannel.sendMessage("Repeat mode is now activated!").queue();
        }
    }

    @Override
    public String getName() {
        return "Repeat";
    }

    @Override
    public String getDescription() {
        return "Sets the current track on repeat.";
    }

    @Override
    public Permission getPermission() {
        return new DJPermission();
    }
}
