package de.treona.utilPlugin.commands;

import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;

public class OwnerCommand implements GuildCommand {

    @Override
    public void execute(Member author, Message message, TextChannel textChannel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.YELLOW);
        Guild guild = message.getGuild();
        embedBuilder.setTitle("Owner for: " + guild.getName());
        if (guild.getOwner().equals(author))
            embedBuilder.addField("You are the owner :upside_down:", "Owner: " + guild.getOwner().getEffectiveName(), false);
        else
            embedBuilder.addField("You are not the owner", "Owner: " + guild.getOwner().getEffectiveName(), false);

        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "Owner";
    }

    @Override
    public String getDescription() {
        return "Shows you the guild owner";
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
