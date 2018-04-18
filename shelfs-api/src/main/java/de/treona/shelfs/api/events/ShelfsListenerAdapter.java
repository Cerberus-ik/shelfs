package de.treona.shelfs.api.events;

import de.treona.shelfs.api.events.command.CommandEvent;
import de.treona.shelfs.api.events.command.GuildCommandEvent;
import de.treona.shelfs.api.events.command.PrivateCommandEvent;
import de.treona.shelfs.api.events.reaction.ReactionMessageEvent;
import de.treona.shelfs.api.events.reaction.ReactionMessageReactionAddEvent;
import de.treona.shelfs.api.events.reaction.ReactionMessageSendEvent;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ShelfsListenerAdapter extends ListenerAdapter {

    public void onCommandEvent(CommandEvent event) {
    }

    public void onPrivateCommandEvent(PrivateCommandEvent event) {
    }

    public void onGuildCommandEvent(GuildCommandEvent event) {
    }

    public void onReactionMessageEvent(ReactionMessageEvent event) {
    }

    public void onReactionMessageSendEvent(ReactionMessageSendEvent event) {
    }

    public void onReactionMessageReactionAddEvent(ReactionMessageReactionAddEvent event) {
    }

    @Override
    public void onGenericEvent(Event event) {
        super.onGenericEvent(event);
        if (event instanceof CommandEvent) {
            if (event instanceof PrivateCommand) this.onPrivateCommandEvent((PrivateCommandEvent) event);
            else if (event instanceof GuildCommand) this.onGuildCommandEvent((GuildCommandEvent) event);
            else this.onCommandEvent((CommandEvent) event);
        }
        if (event instanceof ReactionMessageEvent) {
            if (event instanceof ReactionMessageSendEvent)
                onReactionMessageSendEvent((ReactionMessageSendEvent) event);
            else if (event instanceof ReactionMessageReactionAddEvent)
                onReactionMessageReactionAddEvent((ReactionMessageReactionAddEvent) event);
            else onReactionMessageEvent((ReactionMessageEvent) event);
        }
    }
}
