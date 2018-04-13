package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.permission.DJPermission;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class ShuffleCommand implements GuildCommand {

    private AudioController audioController;

    public ShuffleCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        this.audioController.getMusicManager(textChannel.getGuild()).scheduler.shuffle();
        textChannel.sendMessage("Queue got shuffled.").queue();
    }

    @Override
    public String getName() {
        return "Shuffle";
    }

    @Override
    public String getDescription() {
        return "Will shuffle the queue.";
    }

    @Override
    public Permission getPermission() {
        return new DJPermission();
    }
}
