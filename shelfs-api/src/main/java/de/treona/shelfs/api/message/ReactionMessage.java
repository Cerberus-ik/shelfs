package de.treona.shelfs.api.message;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;

import java.util.List;

public interface ReactionMessage {

    void onReaction(MessageReaction reaction);

    List<MessageReaction> getPossibleReactions();

    Message getMessage();


}
