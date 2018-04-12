package de.treona.shelfs.permission;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public interface Permission {

    boolean hasPermission(Member member);

    boolean hasPermission(User user);

}
