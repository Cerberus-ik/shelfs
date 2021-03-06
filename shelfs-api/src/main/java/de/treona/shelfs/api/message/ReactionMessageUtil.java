package de.treona.shelfs.api.message;

import de.treona.shelfs.api.events.ShelfsListenerAdapter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.stream.Collector;

@SuppressWarnings("unused")
public class ReactionMessageUtil extends ShelfsListenerAdapter {

    private static final LinkedHashMap<String, ReactionMessage> messages = new LinkedHashMap<>();

    public static boolean isReactionMessage(Message message) {
        return messages.keySet().contains(message.getId());
    }

    public static void sendMessage(ReactionMessage reactionMessage, MessageChannel messageChannel) {
        sendMessage(reactionMessage, messageChannel, false);
    }

    public static void sendMessage(ReactionMessage reactionMessage, MessageChannel messageChannel, boolean reverse) {
        new Thread(() -> {
            Message message;
            if (reactionMessage.getMessage() == null)
                message = messageChannel.sendMessage(reactionMessage.getMessageEmbed()).complete();
            else
                message = messageChannel.sendMessage(reactionMessage.getMessage()).complete();
            if (reverse)
                reactionMessage.getPossibleReactions().keySet().forEach(reaction -> message.addReaction(reaction).queue());
            else {
                reactionMessage.getPossibleReactions().keySet().stream().collect(Collector.of(
                        ArrayDeque::new,
                        ArrayDeque::addFirst,
                        (d1, d2) -> {
                            d2.addAll(d1);
                            return d2;
                        })
                ).forEach(reaction -> message.addReaction((String) reaction).queue());
            }
            reactionMessage.afterSend();
            messages.put(message.getId(), reactionMessage);
            if (messages.size() > 10000) {
                String messageToRemove = messages.keySet().iterator().next();
                messages.remove(messageToRemove);
            }
        }).start();
    }

    public static ReactionMessage getMessage(String id) {
        return messages.getOrDefault(id, null);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        ReactionMessage reactionMessage = messages.getOrDefault(event.getMessageId(), null);
        if (reactionMessage == null)
            return;
        reactionMessage.onReaction(event.getReaction(), event.getUser());
    }
}
