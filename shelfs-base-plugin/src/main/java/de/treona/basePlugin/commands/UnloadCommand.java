package de.treona.basePlugin.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class UnloadCommand implements GuildCommand, PrivateCommand {

    private Permission permission;

    public UnloadCommand(Permission permission) {
        this.permission = permission;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        this.unload(textChannel, message.getContentRaw());
    }

    @Override
    public void execute(User user, Message message, PrivateChannel privateChannel) {
        this.unload(privateChannel, message.getContentRaw());
    }

    private void unload(MessageChannel channel, String message) {
        String name = this.getPluginName(message);
        List<ShelfsPlugin> plugins = Shelfs.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getPluginDescription().getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
        if (plugins.size() == 0) {
            channel.sendMessage("No plugins found.").queue();
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Plugins found: ").append(plugins.size()).append(System.lineSeparator());
        plugins.forEach(plugin -> {
            boolean isLoaded = Shelfs.getPluginManager().isPluginEnabled(plugin);
            if (isLoaded) {
                stringBuilder.append(plugin.getPluginDescription().getName()).append(" is loaded").append(System.lineSeparator());
            } else {
                stringBuilder.append(plugin.getPluginDescription().getName()).append(" is unloaded").append(System.lineSeparator());
            }
        });
        channel.sendMessage(stringBuilder.toString()).queue();
        Shelfs.getPluginManager().getDisabledPlugins().forEach(plugins::remove);
        if (plugins.size() == 0) {
            channel.sendMessage("No loaded plugins found.").queue();
            return;
        }
        StringBuilder finalStringBuilder = new StringBuilder();
        finalStringBuilder.append("Plugins unloaded: ").append(plugins.size()).append(System.lineSeparator());
        plugins.forEach(plugin -> {
            Shelfs.getPluginManager().disablePlugin(plugin);
            finalStringBuilder.append("Unloaded ").append(plugin.getPluginDescription().getName());
        });
        channel.sendMessage(finalStringBuilder.toString()).queue();
    }

    private String getPluginName(String message) {
        String[] args = message.split(" ");
        if (args.length <= 1) {
            return "";
        }
        args[0] = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }
        return stringBuilder.toString().substring(1, stringBuilder.toString().length() - 1);
    }

    @Override
    public String getName() {
        return "Unload";
    }

    @Override
    public String getDescription() {
        return "Unloads a plugin";
    }

    @Override
    public Permission getPermission() {
        return this.permission;
    }
}
