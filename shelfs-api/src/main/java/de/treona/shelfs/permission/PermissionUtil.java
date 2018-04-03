package de.treona.shelfs.permission;

import de.treona.shelfs.api.Shelfs;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unused"})
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

    /**
     * Will add a @{@link StringPermission} to a user. If you want to add a @{@link RolePermission}
     * use the @{@link net.dv8tion.jda.core.managers.GuildController}.
     *
     * @param user             will get the @{@link StringPermission}.
     * @param stringPermission the @{@link StringPermission} that will get granted to the user.
     */
    public static void addPermission(User user, StringPermission stringPermission) {
        Shelfs.getIoManager().addPermission(user, stringPermission);
    }

    /**
     * Removes the given @{@link StringPermission} again from a user.
     *
     * @param user             that is targeted.
     * @param stringPermission will get removed from the user.
     */
    public static void removePermission(User user, StringPermission stringPermission) {
        Shelfs.getIoManager().removePermission(user, stringPermission.getPermission());
    }

    /**
     * Returns all users that have the given @{@link Permission}
     *
     * @param permission to check for.
     * @return all users that have the given @{@link Permission}
     */
    public static List<User> getUsersWithPermission(Permission permission) {
        return Shelfs.getJda().getUsers().parallelStream().filter(user -> PermissionUtil.hasPermission(user, permission)).collect(Collectors.toList());
    }
}
