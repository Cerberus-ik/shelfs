package de.treona.shelfs.api.events;

import de.treona.shelfs.api.Shelfs;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {

    private String commandPrefix;

    public Listener(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (!event.getMessage().getContentRaw().startsWith(this.commandPrefix)) {
            return;
        }
        Shelfs.getCommandManager().parseCommand(event.getChannel(), event.getMessage());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getMessage().getContentRaw().startsWith(this.commandPrefix)) {
            return;
        }
        Shelfs.getCommandManager().parseCommand(event.getChannel(), event.getMessage());
    }
}
