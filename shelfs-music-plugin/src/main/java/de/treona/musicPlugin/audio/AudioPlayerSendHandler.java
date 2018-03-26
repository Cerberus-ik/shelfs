package de.treona.musicPlugin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioPlayerSendHandler implements AudioSendHandler {

    private AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public boolean canProvide() {
        this.lastFrame = this.audioPlayer.provide();
        return this.lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() {
        if (this.lastFrame.data == null) {
            System.out.println("Send 20ms of null");
        }
        return this.lastFrame.data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
