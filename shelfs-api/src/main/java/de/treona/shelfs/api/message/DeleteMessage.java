package de.treona.shelfs.api.message;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class DeleteMessage {

    private Message messageSend;
    private boolean instantDelete = false;

    public DeleteMessage(String content, MessageChannel channel){
        this(new MessageBuilder(content).build(), channel);
    }

    public DeleteMessage(String content, MessageChannel channel, int millis){
        this(new MessageBuilder(content).build(), channel, millis
        );
    }

    public DeleteMessage(Message message, MessageChannel channel) {
        new Thread(() -> {
            this.messageSend = channel.sendMessage(message).complete();
            if (instantDelete)
                this.messageSend.delete().queue();
        }).start();
    }

    public DeleteMessage(Message message, MessageChannel channel, int millis) {
        new Thread(() -> {
            this.messageSend = channel.sendMessage(message).complete();
            if (this.instantDelete) {
                this.messageSend.delete().complete();
                return;
            }
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.messageSend == null)
                return;
            this.delete();
        }).start();
    }

    public void delete() {
        if (this.messageSend == null) {
            this.instantDelete = true;
            return;
        }
        this.messageSend.delete().queue((success) -> this.messageSend = null);
    }
}
