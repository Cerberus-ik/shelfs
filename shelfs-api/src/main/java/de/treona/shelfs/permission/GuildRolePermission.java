package de.treona.shelfs.permission;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;

public class GuildRolePermission implements Permission {

    private boolean enablePrivateUsage = false;
    private HashMap<Long, Role> roles;

    public GuildRolePermission(HashMap<Long, Role> roles) {
        this.roles = roles;
    }

    public GuildRolePermission(HashMap<Long, Role> roles, boolean enablePrivateUsage) {
        this.roles = roles;
        this.enablePrivateUsage = enablePrivateUsage;
    }

    @Override
    public boolean hasPermission(Member member) {
        if (!this.roles.containsKey(member.getGuild().getIdLong()))
            return false;
        return member.getRoles().contains(this.roles.get(member.getGuild().getIdLong()));
    }

    @Override
    public boolean hasPermission(User user) {
        return this.enablePrivateUsage;
    }
}
