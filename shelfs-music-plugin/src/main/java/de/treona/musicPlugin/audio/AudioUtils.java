package de.treona.musicPlugin.audio;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class AudioUtils {

    public static boolean joinMember(Member member) {
        if (!member.getVoiceState().inVoiceChannel()) {
            return false;
        }
        connect(member.getVoiceState().getChannel());
        return true;
    }

    public static void connect(VoiceChannel voiceChannel) {
        if (voiceChannel == null) {
            return;
        }
        VoiceChannel currentChannel = voiceChannel.getGuild().getAudioManager().getConnectedChannel();
        if (!voiceChannel.equals(currentChannel)) {
            voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
        }
    }
}
