package de.treona.shelfs.api.events.reaction;

import de.treona.shelfs.api.message.ReactionMessage;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import org.jetbrains.annotations.Nullable;

public class ReactionMessageReactionAddEvent extends ReactionMessageEvent {

    private MessageReaction reaction;
    private User user;
    @Nullable
    private Guild guild;

    public ReactionMessageReactionAddEvent(JDA api, ReactionMessage reactionMessage, MessageReaction reaction, User user, @Nullable Guild guild) {
        super(api, reactionMessage);
        this.reaction = reaction;
        this.user = user;
        this.guild = guild;
    }

    public ReactionMessageReactionAddEvent(JDA api, ReactionMessage reactionMessage, MessageReaction reaction, User user) {
        super(api, reactionMessage);
        this.reaction = reaction;
        this.user = user;
        this.guild = null;
    }

    public MessageReaction getReaction() {
        return reaction;
    }

    public User getUser() {
        return user;
    }

    /**
     * Will return the guild if the message was send in a @{@link net.dv8tion.jda.core.entities.TextChannel}.
     *
     * @return the @{@link Guild} where the message was send. Null if the message was send in a {@link net.dv8tion.jda.core.entities.PrivateChannel}
     */
    public Guild getGuild() {
        return guild;
    }

    public boolean wasSendInGuild() {
        return guild != null;
    }
}
