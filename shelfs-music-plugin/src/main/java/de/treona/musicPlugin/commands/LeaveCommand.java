package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.permission.DJPermission;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class LeaveCommand implements GuildCommand {

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        if (!member.getGuild().getAudioManager().isConnected()) {
            return;
        }
        member.getGuild().getAudioManager().closeAudioConnection();
    }

    @Override
    public String getName() {
        return "Leave";
    }

    @Override
    public String getDescription() {
        return "Makes the bot leave his current channel.";
    }

    @Override
    public Permission getPermission() {
        return new DJPermission();
    }
}
