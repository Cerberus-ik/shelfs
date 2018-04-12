package de.treona.utilPlugin.listener;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JoinListener extends ListenerAdapter {

    private ShelfsPlugin plugin;

    public JoinListener(ShelfsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        JSONObject config = this.plugin.getConfig();
        if (!config.has(event.getGuild().getId())) {
            return;
        }
        event.getGuild().getController().addRolesToMember(event.getMember(), this.getRolesForGuild(event.getGuild())).queue();
    }

    private List<Role> getRolesForGuild(Guild guild) {
        List<Role> roles = new ArrayList<>();
        JSONArray guildRoles = this.plugin.getConfig().getJSONArray(guild.getId());
        for (int i = 0; i < guildRoles.length(); i++) {
            roles.add(Shelfs.getJda().getRoleById(guildRoles.getString(i)));
        }
        return roles.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
