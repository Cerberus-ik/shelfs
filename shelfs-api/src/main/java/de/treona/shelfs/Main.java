package de.treona.shelfs;

import de.treona.shelfs.api.Shelfs;

public class Main {

    public static void main(String[] args) {
        Shelfs.loadConfig();
        Shelfs.start();
    }
}
