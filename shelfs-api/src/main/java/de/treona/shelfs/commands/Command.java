package de.treona.shelfs.commands;

import de.treona.shelfs.permission.Permission;

public interface Command {

    String getName();

    String getDescription();

    Permission getPermission();

}
