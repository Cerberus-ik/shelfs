package de.treona.shelfs.config;

import de.treona.shelfs.io.IOType;
import de.treona.shelfs.io.database.DatabaseCredentials;
import net.dv8tion.jda.core.OnlineStatus;

@SuppressWarnings("WeakerAccess")
public class Config {

    public String pluginDirectory;
    public OnlineStatus onlineStatus;
    public String currentGame;
    public String token;
    public String commandPrefix;
    public IOType ioType;
    public DatabaseCredentials databaseCredentials;
    public boolean dynamicLoad;
}
