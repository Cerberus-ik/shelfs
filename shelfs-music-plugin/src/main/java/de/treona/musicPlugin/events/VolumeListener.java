package de.treona.musicPlugin.events;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.AudioUtils;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.config.GuildSettings;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class VolumeListener extends ListenerAdapter {

    private AudioController audioController;
    private ConfigManager configManager;

    public VolumeListener(AudioController audioController, ConfigManager configManager) {
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
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(event.getGuild());
        boolean saveUpdate = false;
        int newVolume = 25;
        if (event.getReactionEmote().getName().equals("\uD83D\uDD0A")) {
            guildMusicManager.player.setVolume(Math.min(125, guildMusicManager.player.getVolume() + 10));
            newVolume = Math.min(125, guildMusicManager.player.getVolume());
            message.editMessage(AudioUtils.buildVolumeMessage(this.audioController.getMusicManager(event.getGuild()), newVolume, true)).queue();
            saveUpdate = true;
        } else if (event.getReactionEmote().getName().equals("\uD83D\uDD09")) {
            guildMusicManager.player.setVolume(Math.max(3, guildMusicManager.player.getVolume() - 10));
            newVolume = Math.max(3, guildMusicManager.player.getVolume());
            message.editMessage(AudioUtils.buildVolumeMessage(this.audioController.getMusicManager(event.getGuild()), newVolume, true)).queue();
            saveUpdate = true;
        }
        event.getReaction().removeReaction(event.getUser()).queue();
        if (saveUpdate) {
            GuildSettings guildSettings = this.configManager.getGuildSettings(event.getGuild());
            guildSettings.setVolume(newVolume);
            this.configManager.saveGuildSettings(event.getGuild(), guildSettings);
        }
    }

}
