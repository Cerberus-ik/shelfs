package de.treona.basePlugin.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.Command;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import de.treona.shelfs.permission.Permission;
import de.treona.shelfs.permission.RolePermission;
import de.treona.shelfs.permission.StringPermission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CommandsCommand implements GuildCommand, PrivateCommand {

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        String[] args = message.getContentRaw().split(" ");
        Object messageObject = this.buildMessage(this.getPredicate(args, textChannel));
        if (messageObject instanceof String) {
            textChannel.sendMessage((String) messageObject).queue();
        } else {
            textChannel.sendMessage((MessageEmbed) messageObject).queue();
        }
    }

    @Override
    public void execute(User user, Message message, PrivateChannel privateChannel) {
        String[] args = message.getContentRaw().split(" ");
        Object messageObject = this.buildMessage(this.getPredicate(args, privateChannel));
        if (messageObject instanceof String) {
            privateChannel.sendMessage((String) messageObject).queue();
        } else {
            privateChannel.sendMessage((MessageEmbed) messageObject).queue();
        }
    }

    private Predicate getPredicate(String[] args, MessageChannel channel) {
        Predicate<? super Command> predicate = command -> true;
        if (args.length > 1) {
            List<ShelfsPlugin> plugins = Shelfs.getPluginManager()
                    .getPlugins().stream()
                    .filter(plugin -> plugin.getPluginDescription().getName().equalsIgnoreCase(args[1]))
                    .collect(Collectors.toList());
            if (plugins.size() == 0) {
                channel.sendMessage("There is no plugin with that name.").queue();
                return command -> false;
            }
            predicate = command -> Shelfs.getCommandManager()
                    .getPluginFromCommand(command)
                    .getPluginDescription().getName().equalsIgnoreCase(args[1]);
        }
        return predicate;
    }

    private Object buildMessage(Predicate<? super Command> predicate) {
        if (Shelfs.getCommandManager().getCommands().stream().filter(predicate).count() > 20) {
            return this.buildSimpleMessage(predicate);
        } else {
            return this.buildEmbedMessage(predicate);
        }
    }

    private String buildSimpleMessage(Predicate<? super Command> predicate) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Command> commands = Shelfs.getCommandManager().getCommands().stream().filter(predicate).collect(Collectors.toList());
        stringBuilder.append("Commands: ")
                .append(commands.size())
                .append(" (")
                .append(Shelfs.getCommandManager().getCommands().size())
                .append(")");
        Shelfs.getCommandManager().getCommands().stream().filter(predicate).forEach(command -> {
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(command.getName())
                    .append(" from: ")
                    .append(Shelfs.getCommandManager().getPluginFromCommand(command).getPluginDescription().getName())
                    .append(System.lineSeparator())
                    .append(command.getDescription())
                    .append(System.lineSeparator())
                    .append(this.getPermissionString(command.getPermission()))
                    .append(System.lineSeparator());
        });
        String buildMessage = stringBuilder.toString();
        if (buildMessage.length() > 1999) {
            return buildMessage.substring(0, 1996) + "...";
        }
        return stringBuilder.toString();
    }

    private MessageEmbed buildEmbedMessage(Predicate<? super Command> predicate) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        List<Command> commands = Shelfs.getCommandManager().getCommands().stream().filter(predicate).collect(Collectors.toList());
        embedBuilder.setTitle("Commands: " + commands.size() + " (" + Shelfs.getCommandManager().getCommands().size() + ")");
        embedBuilder.setColor(Color.ORANGE);
        Shelfs.getCommandManager().getCommands().stream().filter(predicate).forEach(
                command -> embedBuilder.addField(command.getName()
                                + " from: "
                                + Shelfs.getCommandManager().getPluginFromCommand(command).getPluginDescription().getName(),
                        command.getDescription()
                                + System.lineSeparator()
                                + this.getPermissionString(command.getPermission()),
                        false));
        return embedBuilder.build();
    }

    private String getPermissionString(Permission commandPermission) {
        String permission;
        if (commandPermission == null) {
            permission = "Requires no permission";
        } else if (commandPermission instanceof StringPermission) {
            StringPermission stringPermission = (StringPermission) commandPermission;
            permission = "Requires: " + stringPermission.getPermission();
        } else if (commandPermission instanceof RolePermission) {
            RolePermission rolePermission = (RolePermission) commandPermission;
            permission = "Requires the role: " + rolePermission.getRole().getName();
        } else {
            permission = "Requires an unknown permission.";
        }
        return permission;
    }

    @Override
    public String getName() {
        return "Commands";
    }

    @Override
    public String getDescription() {
        return "Shows you all available commands.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
