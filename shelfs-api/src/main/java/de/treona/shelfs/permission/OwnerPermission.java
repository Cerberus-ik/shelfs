package de.treona.shelfs.permission;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class OwnerPermission implements Permission {

    @Override
    public boolean hasPermission(Member member) {
        return member.isOwner();
    }

    @Override
    public boolean hasPermission(User user) {
        return false;
    }
}
