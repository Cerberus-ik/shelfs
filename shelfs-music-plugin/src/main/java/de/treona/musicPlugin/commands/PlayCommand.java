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
            }
            if (player.getPlayingTrack() == null) {
                trackScheduler.nextTrack();
                AudioUtils.joinMember(member);
            }
        } else {
            if (!AudioUtils.joinMember(member) && !member.getGuild().getAudioManager().isConnected()) {
                textChannel.sendMessage("I have no place to play my music :(").queue();
                return;
            }

            if (args.length > 2 && args[1].equalsIgnoreCase("now")) {
                this.audioController.loadAndPlayNow(this.audioController.getMusicManager(member.getGuild()),
                        textChannel, buildIdentifier(args), false);
            } else if (args.length > 2 && args[1].equalsIgnoreCase("next")) {
                this.audioController.loadAndPlayNext(this.audioController.getMusicManager(member.getGuild()),
                        textChannel, buildIdentifier(args), false);
            } else {
                this.audioController.loadAndPlay(this.audioController.getMusicManager(member.getGuild()),
                        textChannel, buildIdentifier(args), false);
            }
        }
    }

    private String buildIdentifier(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean skipFirstArg = false;
        if (args.length > 2 && args[1].equalsIgnoreCase("now")) {
            skipFirstArg = true;
        } else if (args.length > 2 && args[1].equalsIgnoreCase("next")) {
            skipFirstArg = true;
        }
        if (skipFirstArg) {
            for (int i = 2; i < args.length; i++) {
                stringBuilder.append(args[i]).append(" ");
            }
        } else {
            for (int i = 1; i < args.length; i++) {
                stringBuilder.append(args[i]).append(" ");
            }
        }
        return stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
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
