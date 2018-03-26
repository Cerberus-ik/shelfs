package de.treona.basePlugin.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.commands.PrivateCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;

public class VersionCommand implements GuildCommand, PrivateCommand {

    private String version;

    public VersionCommand(String version) {
        this.version = version;
    }

    @Override
    public void execute(Member author, Message message, TextChannel textChannel) {
        textChannel.sendMessage(this.buildMessage()).queue();
    }

    @Override
    public void execute(User author, Message message, PrivateChannel textChannel) {
        textChannel.sendMessage(this.buildMessage()).queue();
    }

    private MessageEmbed buildMessage() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.BLUE);
        embedBuilder.setTitle("Running Shelfs.");
        embedBuilder.addField("JDA-Version", "Version: " + JDAInfo.VERSION, false);
        embedBuilder.addField("Shelfs-Version", "Version: " + Shelfs.getVersion(), false);
        embedBuilder.addField("Base-Plugin-Version", "Version: " + this.version, false);
        return embedBuilder.build();
    }

    @Override
    public String getName() {
        return "Version";
    }

    @Override
    public String getDescription() {
        return "Shows the version of the bot.";
    }

    @Override
    public Permission getPermission() {
        return null;
    }

}
