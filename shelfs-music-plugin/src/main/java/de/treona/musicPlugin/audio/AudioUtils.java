package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.concurrent.TimeUnit;

public class AudioUtils {

    public static VoiceChannel findMemberVoiceChannel(Member member) {
        if (member.getVoiceState().inVoiceChannel()) {
            return member.getVoiceState().getChannel();
        }
        return null;
    }

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

    public static void sendInfoToDJ(TextChannel channel, AudioTrack track) {
        if (track.getPosition() == 0) {
            if (track.getInfo().isStream) {
                channel.sendMessage("Streaming: ``" + track.getInfo().title + "``").queue();
            } else {
                channel.sendMessage("Playing: ``" + track.getInfo().title + "( " + formatTrackTime(track.getDuration()) + ")``").queue();
            }
        } else {
            if (track.getInfo().isStream) {
                channel.sendMessage("Queued the stream: ``" + track.getInfo().title + "`` Position: " + track.getPosition()).queue();
            } else {
                channel.sendMessage("Queued: ``" + track.getInfo().title + "( " + formatTrackTime(track.getDuration()) + ")`` Position: " + track.getPosition()).queue();
            }
        }
    }

    static void sendInfoTrackTooLong(TextChannel channel, AudioTrack track) {
        channel.sendMessage("The track:" + track.getInfo().title + " is too long (" + TimeUnit.MILLISECONDS.toSeconds(track.getDuration()) + ")").queue();
    }

    private static String formatTrackTime(long trackTime) {
        if (trackTime < 60000) {
            return trackTime / 1000 + " seconds";
        } else {
            long minutes = Math.floorDiv(trackTime / 1000, 60);
            long seconds = trackTime / 1000 % 60;
            return minutes + ":" + seconds;
        }
    }

    public static String formatDuration(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    public static String getArguments(String message) {
        String[] args = message.split(" ");
        if (args.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            stringBuilder.append(args[i]);
            if (i - 1 != args.length) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }
}
