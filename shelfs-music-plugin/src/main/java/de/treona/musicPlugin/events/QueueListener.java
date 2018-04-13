package de.treona.musicPlugin.events;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.musicPlugin.util.AudioMessageUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;

public class QueueListener extends ListenerAdapter {

    private AudioController audioController;

    public QueueListener(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (!AudioPermissionUtil.hasAudioPermission(event.getMember())) {
            return;
        }
        Message message = event.getChannel().getMessageById(event.getMessageId()).complete();
        if (!message.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }
        if (message.getEmbeds().size() == 0) {
            return;
        }
        MessageEmbed messageEmbed = message.getEmbeds().get(0);
        if (messageEmbed.getTitle() == null || !messageEmbed.getTitle().contains("Queue")) {
            return;
        }
        String[] numbers = messageEmbed.getFooter().getText().replaceAll("Site:", "").replaceAll(" ", "").split("/");
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(event.getGuild());
        int currentSite = Integer.parseInt(numbers[0]);
        int sites = (int) Math.ceil(guildMusicManager.scheduler.queue.size() / 10) + 1;
        currentSite = Math.min(currentSite, sites);
        if (sites == 0) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }
        if (event.getReactionEmote().getName().equals("◀")) {
            if (currentSite != 1) {
                message.editMessage(AudioMessageUtils.buildQueueMessage(new ArrayList<>(guildMusicManager.scheduler.queue), currentSite - 1, event.getChannel())).queue();
            }
        } else if (event.getReactionEmote().getName().equals("▶")) {
            if (currentSite != sites) {
                message.editMessage(AudioMessageUtils.buildQueueMessage(new ArrayList<>(guildMusicManager.scheduler.queue), currentSite + 1, event.getChannel())).queue();
            }
        }
        event.getReaction().removeReaction(event.getUser()).queue();
    }

}
