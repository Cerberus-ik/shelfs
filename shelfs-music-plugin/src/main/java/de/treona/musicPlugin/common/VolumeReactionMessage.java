package de.treona.musicPlugin.common;

import de.treona.shelfs.api.message.ReactionMessage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;

import java.util.HashMap;

public class VolumeReactionMessage implements ReactionMessage {

    private ReactionMessage reactionMessage;

    public VolumeReactionMessage(ReactionMessage reactionMessage) {
        this.reactionMessage = reactionMessage;
    }

    @Override
    public void onReaction(MessageReaction reaction) {
        this.reactionMessage.onReaction(reaction);
    }

    @Override
    public HashMap<String, Runnable> getPossibleReactions() {
        return this.reactionMessage.getPossibleReactions();
    }

    @Override
    public Message getMessage() {
        return this.reactionMessage.getMessage();
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        return this.reactionMessage.getMessageEmbed();
    }
}
