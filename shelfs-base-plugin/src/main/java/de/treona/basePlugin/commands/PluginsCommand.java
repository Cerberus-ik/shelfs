package de.treona.basePlugin.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;

public class PluginsCommand implements GuildCommand, PrivateCommand {

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        textChannel.sendMessage(this.buildMessage()).queue();
    }

    @Override
    public void execute(User user, Message message, PrivateChannel privateChannel) {
        privateChannel.sendMessage(this.buildMessage()).queue();
    }

    private MessageEmbed buildMessage() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setTitle("Shelfs");
        Shelfs.getPluginManager().getPlugins().forEach(plugin -> {
            String loaded;
            if (Shelfs.getPluginManager().isPluginLoaded(plugin)) {
                loaded = "Loaded";
            } else {
                loaded = "Unloaded";
            }
            embedBuilder.addField(plugin.getPluginDescription().getName() + " v" + plugin.getPluginDescription().getVersion(), loaded, true);
        });
        return embedBuilder.build();
    }

    @Override
    public String getName() {
        return "Plugins";
    }

    @Override
    public String getDescription() {
        return "Shows a list of installed plugins.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
