package de.treona.shelfs.api.events.command;

import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.GuildCommand;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class GuildCommandEvent extends CommandEvent {

    private Guild guild;
    private GuildCommand command;

    public GuildCommandEvent(JDA api, GuildCommand command, ShelfsPlugin plugin, User user, Guild guild) {
        super(api, command, plugin, user);
        this.command = command;
        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }

    public GuildCommand getGuildCommand() {
        return this.command;
    }
}
