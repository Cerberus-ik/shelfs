package de.treona.shelfs.api.events.command;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.PrivateCommand;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

public class PrivateCommandEvent extends CommandEvent {

    private PrivateCommand command;

    public PrivateCommandEvent(JDA api, PrivateCommand command, ShelfsPlugin plugin, User user) {
        super(api, command, plugin, user);
        this.command = command;
    }

    public PrivateCommand getCommand() {
        return command;
    }
}
