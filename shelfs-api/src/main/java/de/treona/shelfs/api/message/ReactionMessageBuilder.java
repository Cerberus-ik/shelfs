package de.treona.shelfs.api.message;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;

@SuppressWarnings("unused")
public class ReactionMessageBuilder {

    private HashMap<String, ReactionMessageRunnable> reactions;
    private Message message;
    private MessageEmbed messageEmbed;
    private Runnable afterSendRunnable;

    public ReactionMessageBuilder() {
        this.reactions = new HashMap<>();
        this.message = null;
        this.messageEmbed = null;
    }

    public void setAfterSendRunnable(Runnable afterSendRunnable) {
        this.afterSendRunnable = afterSendRunnable;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setMessage(MessageEmbed messageEmbed) {
        this.messageEmbed = messageEmbed;
    }

    public void addReaction(String reaction, ReactionMessageRunnable runnable) {
        if (!this.reactions.containsKey(reaction)) {
            this.reactions.put(reaction, runnable);
        }
    }

    public void addReactions(HashMap<String, ReactionMessageRunnable> reactionsToAdd) {
        reactionsToAdd.keySet().forEach(key -> this.addReaction(key, reactionsToAdd.get(key)));
    }

    public void clearReactions() {
        this.reactions.clear();
    }

    public ReactionMessage build() {
        if (this.reactions.size() == 0) {
            throw new IllegalArgumentException("Message contains no reactions.");
        }
        if (this.message == null && this.messageEmbed == null) {
            throw new IllegalArgumentException("ReactionMessage has no message attached to it.");
        }
        return new ReactionMessage() {
            @Override
            public void onReaction(MessageReaction reaction, User user) {
                this.getPossibleReactions().get(reaction.getReactionEmote().getName()).run(reaction, user);
            }

            @Override
            public void afterSend() {
                if (afterSendRunnable != null) afterSendRunnable.run();
            }

            @Override
            public HashMap<String, ReactionMessageRunnable> getPossibleReactions() {
                return reactions;
            }

            @Override
            public Message getMessage() {
                return message;
            }

            @Override
            public MessageEmbed getMessageEmbed() {
                return messageEmbed;
            }
        };
    }
}
