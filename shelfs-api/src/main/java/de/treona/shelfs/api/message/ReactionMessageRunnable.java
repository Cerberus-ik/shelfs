package de.treona.shelfs.api.message;

import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

public interface ReactionMessageRunnable {

    void run(MessageReaction reaction, User user);
}
