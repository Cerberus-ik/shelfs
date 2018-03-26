package de.treona.shelfs.io.logger;

public enum LogReason {

    BOT("Bot"),
    COMMAND("Command"),
    GUILD("Guild"),
    API("Api"),
    CACHE("Cache"),
    DB("DB"),
    UNKNOWN("Unknown");

    private String s;

    LogReason(String s) {
        this.s = s;
    }

    public String getName() {
        return s;
    }
}
