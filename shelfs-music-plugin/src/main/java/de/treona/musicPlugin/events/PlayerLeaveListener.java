package de.treona.musicPlugin.events;

import de.treona.musicPlugin.audio.AudioController;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class PlayerLeaveListener extends ListenerAdapter {

    private AudioController audioController;

    public PlayerLeaveListener(AudioController audioController) {
        this.audioController = audioController;
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        this.startLeaveCheck(event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        this.startLeaveCheck(event.getChannelLeft());
    }

    private void startLeaveCheck(VoiceChannel voiceChannel) {
        if (!voiceChannel.getGuild().getAudioManager().isConnected()) {
            return;
        }
        if (!voiceChannel.equals(voiceChannel.getGuild().getAudioManager().getConnectedChannel())) {
            return;
        }
        if (voiceChannel.getMembers().stream().anyMatch(member -> !member.getUser().isBot())) {
            return;
        }
        this.audioController.getMusicManager(voiceChannel.getGuild()).scheduler.startLeaveRunnable(15000);
    }
}
