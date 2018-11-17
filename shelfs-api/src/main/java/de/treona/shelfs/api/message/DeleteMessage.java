package de.treona.shelfs.api.message;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.concurrent.Callable;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DeleteMessage {

    private Message messageSend;
    private boolean instantDelete = false;
    private Callable callable;

    public DeleteMessage(String content, MessageChannel channel, Callable callable){
        this(new MessageBuilder(content).build(), channel);
        this.callable = callable;
    }

    public DeleteMessage(String content, MessageChannel channel, int millis, Callable callable){
        this(new MessageBuilder(content).build(), channel, millis);
        this.callable = callable;
    }

    public DeleteMessage(Message message, MessageChannel channel, Callable callable) {
        this(message, channel);
        this.callable = callable;
    }

    public DeleteMessage(Message message, MessageChannel channel, int millis, Callable callable) {
        this(message, channel, millis);
        this.callable = callable;
    }

    public DeleteMessage(String content, MessageChannel channel){
        this(new MessageBuilder(content).build(), channel);
    }

    public DeleteMessage(String content, MessageChannel channel, int millis){
        this(new MessageBuilder(content).build(), channel, millis);
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

    public Object get() throws Exception {
        Object result = this.callable.call();
        this.delete();
        return result;
    }

    public void delete() {
        if (this.messageSend == null) {
            this.instantDelete = true;
            return;
        }
        this.messageSend.delete().queue((success) -> this.messageSend = null);
    }
}
