package de.treona.shelfs.permission;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class RolePermission implements Permission {

    private Role role;

    public RolePermission(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean hasPermission(Member member) {
        return member.getRoles().contains(this.role);
    }

    @Override
    public boolean hasPermission(User user) {
        return false;
    }
}
