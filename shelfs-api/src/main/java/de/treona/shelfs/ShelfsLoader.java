package de.treona.shelfs;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.logger.Logger;

import java.util.Arrays;

public class ShelfsLoader {

    public static void main(String[] args) {
        Logger logger = new Logger("ShelfsLoader");
        if (Arrays.stream(args).anyMatch(arg -> arg.equals("-builder")))
            start();
        else if (Arrays.stream(args).anyMatch(arg -> arg.equals("-bypass"))) {
            logger.logMessage("You are bypassing the build process, this might cause issues!", LogLevel.WARNING);
            start();
        } else
            logger.logMessage("Please launch the bot over the shelfs bot builder.", LogLevel.INFO);
    }

    static void start() {
        Shelfs.loadConfig();
        Shelfs.start();
    }
}
