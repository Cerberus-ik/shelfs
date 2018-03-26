package de.treona.shelfs.io;

import net.dv8tion.jda.core.entities.User;

import java.util.List;

public interface IOManager {

    boolean setupEnvironment();

    void addPermission(User user, String permission);

    void removePermission(User user, String permission);

    boolean hasPermission(User user, String permission);

    List<String> getPermissions(User user);

    String getPluginVar(String pluginName);

    void setPluginVar(String pluginName, String pluginVar);
}
