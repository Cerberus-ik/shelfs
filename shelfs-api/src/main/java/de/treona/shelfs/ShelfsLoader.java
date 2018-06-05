package de.treona.shelfs;

import de.treona.shelfs.api.Shelfs;

import java.util.Arrays;

public class ShelfsLoader {

    public static void main(String[] args) {
        if (Arrays.stream(args).anyMatch(arg -> arg.equals("-builder"))) {
            Shelfs.loadConfig();
            Shelfs.start();
        } else if (Arrays.stream(args).anyMatch(arg -> arg.equals("-bypass"))) {
            System.out.println("You are bypassing the build process, this might cause issues!");
            Shelfs.loadConfig();
            Shelfs.start();
        } else
            System.out.println("Please launch the bot with over the shelfs bot builder.");

    }
}
