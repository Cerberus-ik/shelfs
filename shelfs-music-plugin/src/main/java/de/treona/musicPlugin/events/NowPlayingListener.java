package de.treona.musicPlugin.events;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class NowPlayingListener extends ListenerAdapter {

    private AudioController audioController;

    public NowPlayingListener(AudioController audioController) {
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
        if (messageEmbed.getTitle() == null || !messageEmbed.getTitle().contains("from:")) {
            return;
        }
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(event.getGuild());
        switch (event.getReactionEmote().getName()) {
            case "\u23F8":
                this.switchPause(event.getGuild());
                break;
            case "▶":
                this.switchPause(event.getGuild());
                break;
            case "\uD83D\uDD01":
                this.switchRepeat(event.getGuild());
                break;
            case "\uD83D\uDD02":
                this.switchRepeat(event.getGuild());
                break;
            case "⏩":
                guildMusicManager.scheduler.nextTrack();
                break;
            default:
                event.getReaction().removeReaction().queue();
                return;
        }
        new Thread(() -> {
            message.clearReactions().queue();
            if (audioController.getMusicManager(message.getGuild()).player.isPaused())
                message.addReaction("▶").queue();
            else
                message.addReaction("\u23F8").queue();
            if (audioController.getMusicManager(message.getGuild()).scheduler.isRepeating())
                message.addReaction("\uD83D\uDD02").queue();
            else
                message.addReaction("\uD83D\uDD01").queue();
            message.addReaction("⏩").queue();
            //NowPlayingCommand.addReactions(message, this.audioController);
        }).start();
    }

    private void switchRepeat(Guild guild) {
        this.audioController
                .getMusicManager(guild)
                .scheduler
                .setRepeating(!this.audioController.getMusicManager(guild)
                        .scheduler.isRepeating());
    }

    private void switchPause(Guild guild) {
        this.audioController
                .getMusicManager(guild)
                .player
                .setPaused(!this.audioController.getMusicManager(guild)
                        .player.isPaused());
    }
}
