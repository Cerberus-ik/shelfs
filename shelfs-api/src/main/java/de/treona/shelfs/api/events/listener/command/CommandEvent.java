package de.treona.shelfs.api.events.listener.command;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.Command;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

public class CommandEvent extends Event {

    private Command command;
    private ShelfsPlugin plugin;
    private User user;

    public CommandEvent(JDA api, Command command, ShelfsPlugin plugin, User user) {
        super(api);
        this.command = command;
        this.plugin = plugin;
        this.user = user;
    }

    public Command getCommand() {
        return command;
    }

    public ShelfsPlugin getPlugin() {
        return plugin;
    }

    public User getUser() {
        return user;
    }
}
