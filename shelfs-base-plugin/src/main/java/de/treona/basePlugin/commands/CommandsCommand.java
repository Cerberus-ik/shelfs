package de.treona.basePlugin.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import de.treona.shelfs.permission.Permission;
import de.treona.shelfs.permission.RolePermission;
import de.treona.shelfs.permission.StringPermission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;

public class CommandsCommand implements GuildCommand, PrivateCommand {

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        Object messageObject = this.buildMessage();
        if (messageObject instanceof String) {
            textChannel.sendMessage((String) messageObject).queue();
        } else {
            textChannel.sendMessage((MessageEmbed) messageObject).queue();
        }
    }

    @Override
    public void execute(User user, Message message, PrivateChannel privateChannel) {
        Object messageObject = this.buildMessage();
        if (messageObject instanceof String) {
            privateChannel.sendMessage((String) messageObject).queue();
        } else {
            privateChannel.sendMessage((MessageEmbed) messageObject).queue();
        }
    }

    private Object buildMessage() {
        if (Shelfs.getCommandManager().getCommands().size() > 20) {
            return this.buildSimpleMessage();
        } else {
            return this.buildEmbedMessage();
        }
    }

    private String buildSimpleMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Available commands: ").append(Shelfs.getCommandManager().getCommands().size());
        Shelfs.getCommandManager().getCommands().forEach(command -> {
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(command.getName())
                    .append(" from: ")
                    .append(Shelfs.getCommandManager().getPluginFromCommand(command).getPluginDescription().getName())
                    .append(command.getDescription())
                    .append(System.lineSeparator())
                    .append(this.getPermissionString(command.getPermission()));
        });
        return stringBuilder.toString();
    }

    private MessageEmbed buildEmbedMessage() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Available commands: " + Shelfs.getCommandManager().getCommands().size());
        embedBuilder.setColor(Color.ORANGE);
        Shelfs.getCommandManager().getCommands().stream().filter(command -> command.getName() != null && command.getDescription() != null).forEach(
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
