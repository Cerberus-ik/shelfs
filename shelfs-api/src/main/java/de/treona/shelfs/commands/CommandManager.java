package de.treona.shelfs.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.events.ShelfsListenerAdapter;
import de.treona.shelfs.api.events.command.GuildCommandEvent;
import de.treona.shelfs.api.events.command.PrivateCommandEvent;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CommandManager extends ShelfsListenerAdapter {

    private HashMap<CommandData, Command> commands;
    private String commandPrefix;

    public CommandManager() {
        this.commands = new HashMap<>();
    }

    public List<Command> getCommands() {
        return new ArrayList<>(this.commands.values());
    }

    private CommandData getCommandDataFromCommand(Command command) {
        CommandData commandData = this.commands.keySet().stream().filter(streamCommandData -> this.commands.get(streamCommandData).equals(command)).findAny().orElse(null);
        if (commandData == null) {
            return null;
        }
        return commandData;
    }

    public ShelfsPlugin getPluginFromCommand(Command command) {
        CommandData commandData = this.commands.keySet().stream().filter(streamCommandData -> this.commands.get(streamCommandData).equals(command)).findAny().orElse(null);
        if (commandData == null) {
            return null;
        }
        return commandData.plugin;
    }

    public String getCommandName(Command command) {
        return this.commands.get(this.getCommandDataFromCommand(command)).getName();
    }

    public String getCommandDescription(Command command) {
        return this.commands.get(this.getCommandDataFromCommand(command)).getDescription();
    }

    public Permission getCommandPermission(Command command) {
        return this.commands.get(this.getCommandDataFromCommand(command)).getPermission();
    }

    public void registerCommand(ShelfsPlugin plugin, String trigger, Command command) {
        if (this.commands.values().contains(command)) {
            throw new IllegalStateException("Command is already registered.");
        }
        if (this.commands.keySet().stream().anyMatch(commandData -> commandData.trigger.equals(trigger))) {
            throw new IllegalStateException("Trigger is already registered.");
        }
        this.commands.put(new CommandData(plugin, trigger, command), command);
    }

    public void unregisterCommand(String trigger) {
        if (this.commands.keySet().stream().noneMatch(commandData -> commandData.trigger.equals(trigger))) {
            throw new IllegalStateException("Command is not registered.");
        }
        this.commands.keySet().stream().filter(commandData -> commandData.trigger.equals(trigger)).forEach(this.commands::remove);
    }

    public void unregisterCommand(ShelfsPlugin shelfsPlugin) {
        if (this.commands.keySet().stream().noneMatch(commandData -> commandData.plugin.equals(shelfsPlugin))) {
            return;
        }
        List<CommandData> commandsToUnload = new ArrayList<>();
        this.commands.keySet().stream().filter(commandData -> commandData.plugin.equals(shelfsPlugin)).forEach(commandsToUnload::add);
        commandsToUnload.forEach(this.commands::remove);
    }

    public void parseCommand(MessageChannel channel, Message message) {
        if (message.getContentRaw().length() < 2) {
            return;
        }
        String rawMessage[] = message.getContentRaw().split(" ");
        if (!rawMessage[0].startsWith(this.commandPrefix)) {
            return;
        }
        String trigger = rawMessage[0].replaceFirst(this.commandPrefix, "");
        if (this.commands.values().stream().noneMatch(command -> this.isCorrectCommandType(command, channel))
                || this.commands.keySet().stream().noneMatch(commandData -> commandData.trigger.equalsIgnoreCase(trigger))) {
            return;
        }
        CommandData commandData = this.commands
                .keySet()
                .stream()
                .filter(commandDataStream -> commandDataStream.trigger.equalsIgnoreCase(trigger))
                .findAny()
                .orElse(null);
        Command command = this.commands.get(commandData);
        if (command.getPermission() != null && !command.getPermission().hasPermission(message.getMember())) {
            channel.sendMessage("Sorry but you don't have enough permission to execute this command.").queue();
            return;
        }
        if (commandData == null) {
            throw new NullPointerException("Command data is null.");
        }
        if (channel instanceof TextChannel) {
            ((GuildCommand) command).execute(message.getMember(), message, (TextChannel) channel);
            Shelfs.getJda().getRegisteredListeners().forEach(listener -> {
                ListenerAdapter adapter = (ListenerAdapter) listener;
                adapter.onEvent(new GuildCommandEvent(channel.getJDA(), (GuildCommand) command, commandData.plugin, message.getAuthor(), message.getGuild()));
            });
        } else {
            ((PrivateCommand) command).execute(message.getAuthor(), message, (PrivateChannel) channel);
            Shelfs.getJda().getRegisteredListeners().forEach(listener -> {
                ListenerAdapter adapter = (ListenerAdapter) listener;
                adapter.onEvent(new PrivateCommandEvent(channel.getJDA(), (PrivateCommand) command, commandData.plugin, message.getAuthor()));
            });
        }
    }

    private boolean isCorrectCommandType(Command command, MessageChannel messageChannel) {
        if (messageChannel instanceof PrivateChannel) {
            return command instanceof PrivateCommand;
        } else if (messageChannel instanceof TextChannel) {
            return command instanceof GuildCommand;
        }
        return false;
    }

    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    private class CommandData {
        ShelfsPlugin plugin;
        String trigger;
        Command command;

        CommandData(ShelfsPlugin plugin, String trigger, Command command) {
            this.plugin = plugin;
            this.trigger = trigger;
            this.command = command;
        }
    }
}
