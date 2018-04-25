package de.treona.musicPlugin.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.musicPlugin.audio.GuildMusicManager;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.Queue;

public class QueueUtil {

    public static int sites(Queue<AudioTrack> queue) {
        return (int) Math.ceil(queue.size() / 10) + 1;
    }

    public static int getCurrentSite(Message message, GuildMusicManager guildMusicManager) {
        MessageEmbed messageEmbed = message.getEmbeds().get(0);
        if (messageEmbed.getTitle() == null || !messageEmbed.getTitle().contains("Queue"))
            return -1;
        String[] numbers = messageEmbed.getFooter().getText().replaceAll("Site:", "").replaceAll(" ", "").split("/");
        int currentSite = Integer.parseInt(numbers[0]);
        int sites = (int) Math.ceil(guildMusicManager.scheduler.queue.size() / 10) + 1;
        return Math.min(currentSite, sites);
    }
}
