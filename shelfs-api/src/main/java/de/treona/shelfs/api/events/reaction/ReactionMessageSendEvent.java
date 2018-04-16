package de.treona.shelfs.api.events.reaction;

import de.treona.shelfs.api.message.ReactionMessage;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;

public class ReactionMessageSendEvent extends ReactionMessageEvent {

    private MessageChannel channel;

    public ReactionMessageSendEvent(JDA api, ReactionMessage reactionMessage, MessageChannel messageChannel) {
        super(api, reactionMessage);
        this.channel = messageChannel;
    }

    public MessageChannel getChannel() {
        return channel;
    }
}
