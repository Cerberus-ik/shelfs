package de.treona.shelfs.api.events.reaction;

import de.treona.shelfs.api.message.ReactionMessage;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.Event;

public class ReactionMessageEvent extends Event {

    private ReactionMessage reactionMessage;

    public ReactionMessageEvent(JDA api, ReactionMessage reactionMessage) {
        super(api);
        this.reactionMessage = reactionMessage;
    }

    public ReactionMessage getReactionMessage() {
        return reactionMessage;
    }
}
