package de.treona.shelfs.api.events.listener.reactionMessage;

import de.treona.shelfs.api.events.ShelfsListenerAdapter;
import de.treona.shelfs.api.events.reaction.ReactionMessageReactionAddEvent;
import de.treona.shelfs.api.events.reaction.ReactionMessageSendEvent;
import de.treona.shelfs.api.message.ReactionMessage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

public class ReactionMessageListener extends ShelfsListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        new Thread(() -> {
            Message message = event.getChannel().getMessageById(event.getMessageId()).complete();
            if (message instanceof ReactionMessage) {
                ReactionMessage reactionMessage = (ReactionMessage) message;
                reactionMessage.onReaction(event.getReaction());
                super.onReactionMessageReactionAddEvent(new ReactionMessageReactionAddEvent(event.getJDA(), reactionMessage, event.getReaction(), event.getUser(), event.getGuild()));
            }
        }).start();
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }
        if (event.getMessage() instanceof ReactionMessage) {
            super.onReactionMessageSendEvent(new ReactionMessageSendEvent(event.getJDA(), (ReactionMessage) event.getMessage(), event.getChannel()));
        }
    }
}
