package de.treona.shelfs.io.database;

public interface DatabaseCredentials {

    int getPort();

    String getHost();

    String getDatabase();

    String getUser();

    String getPassword();
}
