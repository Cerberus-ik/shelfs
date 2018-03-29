package de.treona.utilPlugin.commands;

import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.stream.Collectors;

public class ClearCommand implements GuildCommand, PrivateCommand {

    @SuppressWarnings("WeakerAccess")
    public static void clearMessage(MessageChannel messageChannel, boolean clearOnlyOwnMessage) {
        MessageHistory messageHistory = messageChannel.getHistory();
        while (true) {
            List<Message> messages = messageHistory.retrievePast(100).complete();
            if (messages.size() == 0) {
                return;
            }
            if (clearOnlyOwnMessage) {
                messages = messages.stream().filter(message -> message.getAuthor().equals(message.getJDA().getSelfUser())).collect(Collectors.toList());
            }
            messages.forEach(message -> message.delete().queue());
        }
    }

    @Override
    public void execute(Member author, Message message, TextChannel textChannel) {
        boolean clearOnlyOwnMessages = false;
        String[] args = message.getContentRaw().split(" ");
        if (args.length > 1 && args[1].equalsIgnoreCase("own")) {
            clearOnlyOwnMessages = true;
        }
        clearMessage(textChannel, clearOnlyOwnMessages);
    }

    @Override
    public void execute(User author, Message message, PrivateChannel textChannel) {
        clearMessage(textChannel, true);
    }

    @Override
    public String getName() {
        return "Clear";
    }

    @Override
    public String getDescription() {
        return "Clears out either all messages or just messages send from the bot.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
