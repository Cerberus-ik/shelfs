package de.treona.musicPlugin.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.config.GuildSettings;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
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
        if (!AudioPermissionUtil.hasAudioPermission(member)) {
            textChannel.sendMessage("Sorry but you don't have the permission to use this command.").queue();
            return;
        }
        String[] args = message.getContentRaw().split(" ");
        AudioPlayer audioPlayer = this.audioController.getMusicManager(member.getGuild()).player;
        if (args.length <= 1) {
            textChannel.sendMessage("Current volume: **" + audioPlayer.getVolume() + "**").queue();
        } else {
            try {
                int newVolume = Math.max(10, Math.min(100, Integer.parseInt(args[1])));
                audioPlayer.setVolume(newVolume);
                GuildSettings guildSettings = this.configManager.getGuildSettings(member.getGuild());
                guildSettings.setVolume(newVolume);
                this.configManager.saveGuildSettings(member.getGuild(), guildSettings);
                textChannel.sendMessage("New audio player volume: ``" + newVolume + "``").queue();
            } catch (NumberFormatException e) {
                textChannel.sendMessage("``" + args[1] + "`` is not a valid volume level. (10 - 100)").queue();
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
        return null;
    }
}
