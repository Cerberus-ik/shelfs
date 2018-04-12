package de.treona.shelfs.permission;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class StringPermission implements Permission {

    private String permission;

    public StringPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public boolean hasPermission(Member member) {
        return PermissionUtil.hasPermission(member.getUser(), this);
    }

    @Override
    public boolean hasPermission(User user) {
        return PermissionUtil.hasPermission(user, this);
    }
}
