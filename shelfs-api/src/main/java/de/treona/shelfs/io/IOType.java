package de.treona.shelfs.io;

public enum IOType {

    MySQL("MySQL");

    private String name;

    IOType(String name) {
        this.name = name;
    }

    public static IOType getTypeByName(String name) {
        return IOType.valueOf(name);
    }

    public String getName() {
        return name;
    }
}
