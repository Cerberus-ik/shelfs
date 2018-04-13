package de.treona.musicPlugin.events;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.musicPlugin.util.AudioMessageUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class SearchListener extends ListenerAdapter {

    private AudioController audioController;
    private ConfigManager configManager;

    public SearchListener(AudioController audioController, ConfigManager configManager) {
        this.audioController = audioController;
        this.configManager = configManager;
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
        if (messageEmbed.getTitle() == null) {
            return;
        }
        if (messageEmbed.getFields().size() == 0) {
            return;
        }
        if (!messageEmbed.getTitle().contains("Found")) {
            return;
        }
        Guild guild = event.getGuild();
        switch (event.getReaction().getReactionEmote().getName()) {
            case "\u0031\u20E3":
                this.play(1, guild);
                break;
            case "\u0032\u20E3":
                this.play(2, guild);
                break;
            case "\u0033\u20E3":
                this.play(3, guild);
                break;
            case "\u0034\u20E3":
                this.play(4, guild);
                break;
            case "\u0035\u20E3":
                this.play(5, guild);
                break;
            case "\u0036\u20E3":
                this.play(6, guild);
                break;
            case "\u0037\u20E3":
                this.play(7, guild);
                break;
            case "\u0038\u20E3":
                this.play(8, guild);
                break;
            case "\u0039\u20E3":
                this.play(9, guild);
                break;
            case "\uD83D\uDD1F":
                this.play(10, guild);
                break;
        }
        event.getReaction().removeReaction(event.getUser()).queue();
    }

    private void play(int song, Guild guild) {
        List<AudioTrack> tracks = this.audioController.getSearchManager().getSearch(guild);
        if (tracks.size() < song) {
            return;
        }
        TextChannel textChannel = this.configManager.getGuildSettings(guild).musicChannel;
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(guild);
        AudioTrack track = tracks.get(song - 1);
        if (textChannel != null) {
            AudioMessageUtils.sendQueueInfo(textChannel, tracks.stream().filter(streamTrack -> streamTrack.equals(track)).collect(Collectors.toList()), guildMusicManager);
        }
        this.audioController.getMusicManager(guild).scheduler.queue(track);
    }
}
