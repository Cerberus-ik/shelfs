package de.treona.shelfs.api.message;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;

import java.util.HashMap;

public interface ReactionMessage {

    void onReaction(MessageReaction reaction);

    HashMap<String, Runnable> getPossibleReactions();

    Message getMessage();

    MessageEmbed getMessageEmbed();
}
