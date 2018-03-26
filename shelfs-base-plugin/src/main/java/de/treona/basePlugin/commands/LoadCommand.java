package de.treona.basePlugin.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import de.treona.shelfs.permission.Permission;
import de.treona.shelfs.permission.RolePermission;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class LoadCommand implements GuildCommand, PrivateCommand {

    private Permission permission;

    public LoadCommand(RolePermission rolePermission) {
        this.permission = rolePermission;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        this.loadPlugin(textChannel, message.getContentRaw());
    }

    @Override
    public void execute(User user, Message message, PrivateChannel privateChannel) {
        this.loadPlugin(privateChannel, message.getContentRaw());
    }

    private void loadPlugin(MessageChannel channel, String message) {
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
            boolean isLoaded = Shelfs.getPluginManager().isPluginLoaded(plugin);
            if (isLoaded) {
                stringBuilder.append(plugin.getPluginDescription().getName()).append(" is loaded").append(System.lineSeparator());
            } else {
                stringBuilder.append(plugin.getPluginDescription().getName()).append(" is unloaded").append(System.lineSeparator());
            }
        });
        channel.sendMessage(stringBuilder.toString()).queue();
        Shelfs.getPluginManager().getLoadedPlugins().forEach(plugins::remove);
        if (plugins.size() == 0) {
            channel.sendMessage("No unloaded plugins found.").queue();
            return;
        }
        StringBuilder finalStringBuilder = new StringBuilder();
        finalStringBuilder.append("Plugins loaded: ").append(plugins.size()).append(System.lineSeparator());
        plugins.forEach(plugin -> {
            Shelfs.getPluginManager().loadPlugin(plugin);
            finalStringBuilder.append("Loaded ").append(plugin.getPluginDescription().getName());
        });
        channel.sendMessage(finalStringBuilder.toString()).queue();
    }

    private String getPluginName(String message) {
        String[] args = message.split(" ");
        if (args.length <= 1) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }
        return stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    }

    @Override
    public String getName() {
        return "Load";
    }

    @Override
    public String getDescription() {
        return "Loads a plugin";
    }

    @Override
    public Permission getPermission() {
        return this.permission;
    }
}
