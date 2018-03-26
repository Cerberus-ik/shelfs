package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class LeaveCommand implements GuildCommand {

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        if (!AudioPermissionUtil.hasAudioPermission(member)) {
            textChannel.sendMessage("Sorry bot you don't have permission to control this bot.").queue();
            return;
        }
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
        return null;
    }
}
