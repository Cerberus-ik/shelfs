package de.treona.shelfs.api.audio;

import com.sedmelluq.discord.lavaplayer.udpqueue.natives.UdpQueueManager;
import net.dv8tion.jda.core.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.core.audio.factory.IPacketProvider;

import java.net.DatagramPacket;

public class ShelfsAudioSendSystem implements IAudioSendSystem {

    private final long queueKey;
    private final ShelfsAudioSendFactory audioSendSystem;
    private final IPacketProvider packetProvider;

    public ShelfsAudioSendSystem(long queueKey, ShelfsAudioSendFactory audioSendSystem, IPacketProvider packetProvider) {
        this.queueKey = queueKey;
        this.audioSendSystem = audioSendSystem;
        this.packetProvider = packetProvider;
    }

    @Override
    public void start() {
        audioSendSystem.addInstance(this);
    }

    @Override
    public void shutdown() {
        audioSendSystem.removeInstance(this);
    }

    void populateQueue(UdpQueueManager queueManager) {
        int remaining = queueManager.getRemainingCapacity(queueKey);
        boolean emptyQueue = queueManager.getCapacity() - remaining > 0;

        for (int i = 0; i < remaining; i++) {
            DatagramPacket packet = packetProvider.getNextPacket(emptyQueue);

            if (packet == null || !queueManager.queuePacket(queueKey, packet)) {
                break;
            }
        }
    }
}
