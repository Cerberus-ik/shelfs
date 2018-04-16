package de.treona.shelfs.api.events.listener.command;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.Command;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

public class PrivateCommandEvent extends CommandEvent {

    public PrivateCommandEvent(JDA api, Command command, ShelfsPlugin plugin, User user) {
        super(api, command, plugin, user);
    }
}
