package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.common.VolumeReactionMessage;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.permission.DJPermission;
import de.treona.musicPlugin.util.AudioMessageUtils;
import de.treona.musicPlugin.util.VolumeUtil;
import de.treona.shelfs.api.message.ReactionMessageUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class VolumeCommand implements GuildCommand {

    private AudioController audioController;
    private ConfigManager configManager;

    public VolumeCommand(AudioController audioController, ConfigManager configManager) {
        this.audioController = audioController;
        this.configManager = configManager;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        String[] args = message.getContentRaw().split(" ");
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(member.getGuild());
        if (args.length <= 1) {
            VolumeReactionMessage reactionMessage = AudioMessageUtils.buildVolumeReactionMessage(guildMusicManager,
                    this.configManager,
                    AudioMessageUtils.buildVolumeMessageEmbed(guildMusicManager.player.getVolume(),
                            guildMusicManager,
                            this.configManager));
            ReactionMessageUtil.sendMessage(reactionMessage, textChannel);
        } else {
            try {
                int newVolume = Math.max(3, Math.min(125, Integer.parseInt(args[1])));
                VolumeUtil.setVolume(newVolume, guildMusicManager, this.configManager);
                VolumeReactionMessage reactionMessage = AudioMessageUtils.buildVolumeReactionMessage(guildMusicManager,
                        this.configManager,
                        AudioMessageUtils.buildVolumeMessageEmbed(guildMusicManager.player.getVolume(),
                                guildMusicManager,
                                this.configManager));
                ReactionMessageUtil.sendMessage(reactionMessage, textChannel);
            } catch (NumberFormatException e) {
                textChannel.sendMessage("``" + args[1] + "`` is not a valid volume level. (" + VolumeUtil.MIN_VOLUME + " - " + VolumeUtil.MAX_VOLUME + ")").queue();
            }
        }
    }


    @Override
    public String getName() {
        return "Volume";
    }

    @Override
    public String getDescription() {
        return "Lets you change the volume of the music bot.";
    }

    @Override
    public Permission getPermission() {
        return new DJPermission();
    }
}
