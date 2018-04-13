package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.config.GuildSettings;
import de.treona.musicPlugin.permission.SettingsPermission;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class SettingsRoleCommand implements GuildCommand {

    private ConfigManager configManager;

    public SettingsRoleCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        List<Role> roles = message.getMentionedRoles();
        if (roles.size() > 1) {
            textChannel.sendMessage("Please only specify one role!").queue();
            return;
        }
        Role role;
        if (roles.size() == 0) {
            Role tempRole = this.getRole(message.getContentRaw(), member.getJDA());
            if (tempRole == null) {
                textChannel.sendMessage("Please mention the role with ``@RoleName`` or simply past the role id.").queue();
                return;
            }
            role = tempRole;
        } else {
            role = roles.get(0);
        }
        if (!member.getGuild().getRoles().contains(role)) {
            textChannel.sendMessage("The role: " + role.getName() + " is not from this server.").queue();
            return;
        }
        GuildSettings guildSettings = this.configManager.getGuildSettings(member.getGuild());
        guildSettings.settingsRole = role;
        this.configManager.saveGuildSettings(member.getGuild(), guildSettings);
        textChannel.sendMessage("Settings got saved successfully.").queue();
    }

    private Role getRole(String message, JDA jda) {
        String[] args = message.split(" ");
        if (args.length < 2) {
            return null;
        }
        try {
            return jda.getRoleById(args[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "Settings Role";
    }

    @Override
    public String getDescription() {
        return "Let's you chose a role that is required to change the bot settings.";
    }

    @Override
    public Permission getPermission() {
        return new SettingsPermission();
    }
}
