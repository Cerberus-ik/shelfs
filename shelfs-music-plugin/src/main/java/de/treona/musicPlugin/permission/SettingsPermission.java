package de.treona.musicPlugin.permission;

import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class SettingsPermission implements Permission {

    @Override
    public boolean hasPermission(Member member) {
        return AudioPermissionUtil.hasSettingsPermission(member);
    }

    @Override
    public boolean hasPermission(User user) {
        return false;
    }
}
