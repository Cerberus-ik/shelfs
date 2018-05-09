package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.AudioUtils;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.util.AudioMessageUtils;
import de.treona.musicPlugin.util.QueueUtil;
import de.treona.shelfs.api.message.ReactionMessageBuilder;
import de.treona.shelfs.api.message.ReactionMessageUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

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
        MessageEmbed messageEmbed = AudioMessageUtils.buildQueueMessage(guildMusicManager.scheduler.queue, 1, textChannel);
        ReactionMessageBuilder reactionMessageBuilder = new ReactionMessageBuilder();
        reactionMessageBuilder.setMessage(messageEmbed);

        reactionMessageBuilder.addReaction("◀", (reaction, user) -> {
            if (!AudioUtils.joinMember(member) && !member.getGuild().getAudioManager().isConnected())
                return;
            new Thread(() -> {
                Message reactedMessage = reaction.getChannel().getMessageById(reaction.getMessageId()).complete();
                int currentSite = QueueUtil.getCurrentSite(reactedMessage, guildMusicManager);
                if (currentSite - 1 < 1) {
                    return;
                }
                reactedMessage.editMessage(AudioMessageUtils.buildQueueMessage(guildMusicManager.scheduler.queue, currentSite - 1, reaction.getChannel())).queue();
            }).start();
        });
        reactionMessageBuilder.addReaction("▶", (reaction, user) -> {
            if (!AudioUtils.joinMember(member) && !member.getGuild().getAudioManager().isConnected())
                return;
            new Thread(() -> {
                Message reactedMessage = reaction.getChannel().getMessageById(reaction.getMessageId()).complete();
                int currentSite = QueueUtil.getCurrentSite(reactedMessage, guildMusicManager);
                if (currentSite + 1 > QueueUtil.sites(guildMusicManager.scheduler.queue)) {
                    return;
                }
                reactedMessage.editMessage(AudioMessageUtils.buildQueueMessage(guildMusicManager.scheduler.queue, currentSite + 1, reaction.getChannel())).queue();
            }).start();
        });
        ReactionMessageUtil.sendMessage(reactionMessageBuilder.build(), textChannel, true);
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
