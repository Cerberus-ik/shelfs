package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioUtils;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.List;

public class JoinCommand implements GuildCommand {

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        if (!AudioPermissionUtil.hasAudioPermission(member)) {
            textChannel.sendMessage("Sorry bot you don't have permission to control this bot.").queue();
            return;
        }
        String[] args = message.getContentRaw().split(" ");
        if (args.length > 1) {
            String channel = args[1];
            try {
                long channelId = Long.parseLong(channel);
                VoiceChannel voiceChannel = member.getGuild().getVoiceChannelById(channelId);
                if (voiceChannel == null) {
                    textChannel.sendMessage("The id matches no voice channel in this guild.").queue();
                    return;
                }
                AudioUtils.connect(voiceChannel);
            } catch (NumberFormatException e) {
                List<VoiceChannel> channels = member.getGuild().getVoiceChannelsByName(channel, true);
                if (channels.size() == 0) {
                    textChannel.sendMessage("There is no voice channel with that name.").queue();
                    return;
                }
                AudioUtils.connect(channels.get(0));
            }
        } else if (member.getVoiceState().inVoiceChannel()) {
            AudioUtils.connect(member.getVoiceState().getChannel());
        } else {
            textChannel.sendMessage("You are currently not in a voice channel.").queue();
        }
    }

    @Override
    public String getName() {
        return "Join";
    }

    @Override
    public String getDescription() {
        return "Lets the bot join you";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
