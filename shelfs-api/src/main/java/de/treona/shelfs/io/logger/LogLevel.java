package de.treona.shelfs.io.logger;

public enum LogLevel {

    INFO("Info"),
    ERROR("Error"),
    WARNING("Warning"),
    UNKNOWN("Unknown");

    private String s;

    LogLevel(String s) {
        this.s = s;
    }

    public String getName() {
        return s;
    }
}
