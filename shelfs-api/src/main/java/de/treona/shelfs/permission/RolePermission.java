package de.treona.shelfs.permission;

import net.dv8tion.jda.core.entities.Role;

public class RolePermission extends Permission {

    private Role role;

    public RolePermission(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }
}
