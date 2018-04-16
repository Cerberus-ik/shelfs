package de.treona.shelfs.api.events.listener.command;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.Command;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class GuildCommandEvent extends CommandEvent {

    private Guild guild;

    public GuildCommandEvent(JDA api, Command command, ShelfsPlugin plugin, User user, Guild guild) {
        super(api, command, plugin, user);
        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }
}
