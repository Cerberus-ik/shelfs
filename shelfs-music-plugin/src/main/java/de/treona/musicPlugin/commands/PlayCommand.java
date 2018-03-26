package de.treona.musicPlugin.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.AudioUtils;
import de.treona.musicPlugin.audio.TrackScheduler;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class PlayCommand implements GuildCommand {

    private AudioController audioController;

    public PlayCommand(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        if (!AudioPermissionUtil.hasAudioPermission(member)) {
            textChannel.sendMessage("Sorry but you don't have the permission to use this command.").queue();
            return;
        }
        String[] args = message.getContentRaw().split(" ");
        AudioPlayer player = this.audioController.getMusicManager(member.getGuild()).player;
        TrackScheduler trackScheduler = this.audioController.getMusicManager(member.getGuild()).scheduler;
        if (args.length <= 1) {
            if (player.isPaused()) {
                player.setPaused(false);
                textChannel.sendMessage("Playback as been resumed.").queue();
            } else if (trackScheduler.queue.isEmpty()) {
                textChannel.sendMessage("The current audio queue is empty! Add something to the queue first!").queue();
            }
        } else {
            if (!AudioUtils.joinMember(member) && !member.getGuild().getAudioManager().isConnected()) {
                textChannel.sendMessage("I have no place to play my music :(").queue();
                return;
            }
            this.audioController.loadAndPlay(this.audioController.getMusicManager(member.getGuild()),
                    textChannel, args[1], false);
        }
    }

    @Override
    public String getName() {
        return "Play";
    }

    @Override
    public String getDescription() {
        return "Lets you play a song or playlist.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
