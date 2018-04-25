package de.treona.shelfs.api.events.listener;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.events.ShelfsListenerAdapter;
import de.treona.shelfs.api.events.reaction.ReactionMessageReactionAddEvent;
import de.treona.shelfs.api.events.reaction.ReactionMessageSendEvent;
import de.treona.shelfs.api.message.ReactionMessage;
import de.treona.shelfs.api.message.ReactionMessageUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ReactionMessageListener extends ShelfsListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        if (event.getUser().equals(Shelfs.getJda().getSelfUser())) {
            return;
        }
        new Thread(() -> {
            Message message = event.getChannel().getMessageById(event.getMessageId()).complete();
            if (ReactionMessageUtil.isReactionMessage(message)) {
                ReactionMessage reactionMessage = ReactionMessageUtil.getMessage(message.getId());
                event.getReaction().removeReaction(event.getUser()).queue();
                if (reactionMessage.getPossibleReactions().keySet().contains(event.getReaction().getReactionEmote().getName())) {
                    reactionMessage.onReaction(event.getReaction(), event.getUser());
                    event.getJDA().getRegisteredListeners().forEach(listener -> {
                        ListenerAdapter adapter = (ListenerAdapter) listener;
                        adapter.onEvent(new ReactionMessageReactionAddEvent(event.getJDA(), reactionMessage, event.getReaction(), event.getUser(), event.getGuild()));
                    });
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }
        if (event.getMessage() instanceof ReactionMessage) {
            event.getJDA().getRegisteredListeners().forEach(listener -> {
                ListenerAdapter adapter = (ListenerAdapter) listener;
                adapter.onEvent(new ReactionMessageSendEvent(event.getJDA(), (ReactionMessage) event.getMessage(), event.getChannel()));
            });
        }
    }
}
