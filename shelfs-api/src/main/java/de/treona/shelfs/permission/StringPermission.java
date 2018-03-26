package de.treona.shelfs.permission;

public class StringPermission extends Permission {

    String permission;

    public StringPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
