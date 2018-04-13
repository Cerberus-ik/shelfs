package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.util.AudioMessageUtils;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;

public class QueueCommand implements GuildCommand {

    private AudioController audioController;

    public QueueCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(member.getGuild());
        if (guildMusicManager.scheduler.queue.size() == 0) {
            textChannel.sendMessage("The queue is empty.").queue();
            return;
        }
        String[] args = message.getContentRaw().split(" ");
        if (args.length == 2 && args[1].equalsIgnoreCase("clear")) {
            guildMusicManager.scheduler.queue.clear();
            textChannel.sendMessage("Queue got cleared.").queue();
            return;
        }
        MessageEmbed messageEmbed = AudioMessageUtils.buildQueueMessage(new ArrayList<>(guildMusicManager.scheduler.queue), 1, textChannel);
        Message sendMessage = textChannel.sendMessage(messageEmbed).complete();
        sendMessage.addReaction("◀").queue();
        sendMessage.addReaction("▶").queue();
//        int elementsToShow = 10;
//        if (guildMusicManager.scheduler.queue.size() < elementsToShow) {
//            elementsToShow = guildMusicManager.scheduler.queue.size();
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("There are ")
//                .append(guildMusicManager.scheduler.queue.size())
//                .append(" tracks in the queue.")
//                .append(System.lineSeparator());
//        int i = 1;
//        for (AudioTrack audioTrack : guildMusicManager.scheduler.queue) {
//            stringBuilder.append(System.lineSeparator())
//                    .append("Queue position: ``")
//                    .append(i)
//                    .append("`` ")
//                    .append(audioTrack.getInfo().title)
//                    .append(" ``(")
//                    .append(AudioMessageUtils.formatDuration(audioTrack.getDuration()))
//                    .append(")``");
//            if (i == elementsToShow) {
//                break;
//            }
//            i++;
//        }
//        textChannel.sendMessage(stringBuilder.toString()).queue();
    }

    @Override
    public String getName() {
        return "Queue";
    }

    @Override
    public String getDescription() {
        return "Shows you the current queue of the music bot.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
