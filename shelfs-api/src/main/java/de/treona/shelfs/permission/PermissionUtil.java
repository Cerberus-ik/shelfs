package de.treona.shelfs.permission;

import de.treona.shelfs.api.Shelfs;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class PermissionUtil {

    /**
     * Depending on the @{@link de.treona.shelfs.io.IOType} this can cause stutters.
     * You might want to call this async.
     *
     * @param user             you want to check if he/she has the given permission.
     * @param stringPermission the permission you want to check for.
     * @return true if the @{@link User} has the give permission and false if not.
     */
    public static boolean hasPermission(User user, StringPermission stringPermission) {
        return Shelfs.getIoManager().hasPermission(user, stringPermission.getPermission());
    }

    /**
     * Checks if a @{@link User} has a given role.
     *
     * @param user           you want to check if he/she has the given permission.
     * @param rolePermission the permission you want to check for.
     * @return true if the @{@link User} has the give permission and false if not.
     */
    public static boolean hasPermission(User user, RolePermission rolePermission) {
        Guild guild = rolePermission.getRole().getGuild();
        Member member = guild.getMember(user);
        if (member == null) {
            return false;
        }
        return member.getRoles().contains(rolePermission.getRole());
    }

    /**
     * Checks if a @{@link Member} has a given role.
     *
     * @param member         you want to check if he/she has the given permission.
     * @param rolePermission the permission you want to check for.
     * @return true if the @{@link Member} has the give permission and false if not.
     */
    public static boolean hasPermission(Member member, RolePermission rolePermission) {
        if (member == null) {
            return false;
        }
        return member.getRoles().contains(rolePermission.getRole());
    }

    /**
     * Checks if a @{@link User} has the given @{@link Permission}
     *
     * @param user       to check the @{@link Permission} for.
     * @param permission to check for.
     * @return true if the user has the given @{@link Permission} false when not.
     */
    public static boolean hasPermission(User user, Permission permission) {
        if (permission == null) {
            return true;
        }
        if (permission instanceof RolePermission) {
            return hasPermission(user, (RolePermission) permission);
        } else if (permission instanceof StringPermission) {
            return hasPermission(user, (StringPermission) permission);
        }
        return false;
    }
}
