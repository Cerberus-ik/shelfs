package de.treona.musicPlugin.events;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.common.VolumeReactionMessage;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.util.AudioMessageUtils;
import de.treona.shelfs.api.events.ShelfsListenerAdapter;
import de.treona.shelfs.api.events.reaction.ReactionMessageReactionAddEvent;
import net.dv8tion.jda.core.entities.Message;

public class VolumeListener extends ShelfsListenerAdapter {

    private AudioController controller;
    private ConfigManager configManager;

    public VolumeListener(AudioController controller, ConfigManager configManager) {
        this.controller = controller;
        this.configManager = configManager;
    }

    @Override
    public void onReactionMessageReactionAddEvent(ReactionMessageReactionAddEvent event) {
        if (event.getReactionMessage() instanceof VolumeReactionMessage) {
            new Thread(() -> {
                Message message = event.getReaction().getChannel().getMessageById(event.getReaction().getMessageId()).complete();
                GuildMusicManager guildMusicManager = this.controller.getMusicManager(event.getGuild());
                message.editMessage(AudioMessageUtils.buildVolumeMessageEmbed(guildMusicManager.player.getVolume(), guildMusicManager, this.configManager)).queue();
            }).start();
        }
    }
}
