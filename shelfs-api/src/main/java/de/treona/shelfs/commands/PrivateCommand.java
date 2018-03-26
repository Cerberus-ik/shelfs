package de.treona.shelfs.commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public interface PrivateCommand extends Command {

    void execute(User author, Message message, PrivateChannel textChannel);
}
