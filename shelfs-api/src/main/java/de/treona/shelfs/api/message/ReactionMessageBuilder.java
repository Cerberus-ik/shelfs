package de.treona.shelfs.api.message;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;

import java.util.HashMap;

@SuppressWarnings("unused")
public class ReactionMessageBuilder {

    private HashMap<String, Runnable> reactions;
    private Message message;
    private MessageEmbed messageEmbed;

    public ReactionMessageBuilder() {
        this.reactions = new HashMap<>();
        this.message = null;
        this.messageEmbed = null;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setMessage(MessageEmbed messageEmbed) {
        this.messageEmbed = messageEmbed;
    }

    public void addReaction(String reaction, Runnable runnable) {
        if (!this.reactions.containsKey(reaction)) {
            this.reactions.put(reaction, runnable);
        }
    }

    public void addReactions(HashMap<String, Runnable> reactionsToAdd) {
        reactionsToAdd.keySet().forEach(key -> this.addReaction(key, reactionsToAdd.get(key)));
    }

    public void clearReactions() {
        this.reactions.clear();
    }

    public ReactionMessage build() {
        if (reactions.size() == 0) {
            throw new IllegalArgumentException("Message contains no reactions.");
        }
        if (message == null && messageEmbed == null) {
            throw new IllegalArgumentException("ReactionMessage has no message attached to it.");
        }
        return new ReactionMessage() {
            @Override
            public void onReaction(MessageReaction reaction) {
                this.getPossibleReactions().get(reaction.getReactionEmote().getName()).run();
            }

            @Override
            public HashMap<String, Runnable> getPossibleReactions() {
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
