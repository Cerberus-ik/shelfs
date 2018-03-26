package de.treona.shelfs.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public interface GuildCommand extends Command {

    void execute(Member author, Message message, TextChannel textChannel);
}
