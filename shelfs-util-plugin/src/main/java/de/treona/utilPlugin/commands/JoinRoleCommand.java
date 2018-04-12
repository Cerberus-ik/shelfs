package de.treona.utilPlugin.commands;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.OwnerPermission;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;

public class JoinRoleCommand implements GuildCommand {

    private ShelfsPlugin shelfsPlugin;

    public JoinRoleCommand(ShelfsPlugin shelfsPlugin) {
        this.shelfsPlugin = shelfsPlugin;
    }

    @Override
    public void execute(Member author, Message message, TextChannel textChannel) {
        List<Role> roles = message.getMentionedRoles();
        String[] args = message.getContentRaw().split(" ");
        JSONObject config = this.shelfsPlugin.getConfig();
        if (roles.size() == 0) {
            if (args.length > 1 && args[1].equalsIgnoreCase("clear")) {
                if (config.has(message.getGuild().getId())) {
                    config.remove(message.getGuild().getId());
                }
                textChannel.sendMessage("No roles will be added to new users.").queue();
            }
            this.sendJoinRoleMessage(textChannel);
            return;
        }
        JSONArray guildArray = new JSONArray();
        for (int i = 0; i < Math.min(25, roles.size()); i++) {
            guildArray.put(roles.get(i).getId());
        }
        config.put(message.getGuild().getId(), guildArray);
        this.shelfsPlugin.saveConfig(config);
        this.sendJoinRoleMessage(textChannel);
    }

    private void sendJoinRoleMessage(TextChannel textChannel) {
        JSONObject config = this.shelfsPlugin.getConfig();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Roles for new users.");
        embedBuilder.setColor(Color.RED);
        if (!config.has(textChannel.getGuild().getId()))
            embedBuilder.addField("", "No roles", false);
        else {
            for (int i = 0; i < config.getJSONArray(textChannel.getGuild().getId()).length(); i++) {
                Role role = Shelfs.getJda().getRoleById(config.getJSONArray(textChannel.getGuild().getId()).getString(i));
                if (role == null)
                    continue;
                embedBuilder.addField("", role.getAsMention(), true);
            }
        }
        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "Join Role";
    }

    @Override
    public String getDescription() {
        return "Adds the given role to every new member.";
    }

    @Override
    public Permission getPermission() {
        return new OwnerPermission();
    }
}
