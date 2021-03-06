package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.GuildMusicManager;
import de.treona.musicPlugin.permission.DJPermission;
import de.treona.musicPlugin.util.AudioMessageUtils;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class ReplayCommand implements GuildCommand {

    private AudioController audioController;

    public ReplayCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        GuildMusicManager guildMusicManager = this.audioController.getMusicManager(member.getGuild());
        if (guildMusicManager.player.getPlayingTrack() != null) {
            guildMusicManager.player.playTrack(guildMusicManager.player.getPlayingTrack().makeClone());
            AudioMessageUtils.sendPlayInfoToDJ(textChannel, guildMusicManager.scheduler.lastTrack.makeClone());
        } else if (guildMusicManager.scheduler.lastTrack == null) {
            textChannel.sendMessage("No track got played so far.").queue();
        } else {
            guildMusicManager.player.playTrack(guildMusicManager.scheduler.lastTrack.makeClone());
            AudioMessageUtils.sendPlayInfoToDJ(textChannel, guildMusicManager.scheduler.lastTrack.makeClone());
        }
    }

    @Override
    public String getName() {
        return "Replay";
    }

    @Override
    public String getDescription() {
        return "Restarts either the current track or the one which was played last.";
    }

    @Override
    public Permission getPermission() {
        return new DJPermission();
    }
}
