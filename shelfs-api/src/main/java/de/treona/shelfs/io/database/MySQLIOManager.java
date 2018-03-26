package de.treona.shelfs.io.database;

import com.mysql.cj.jdbc.MysqlDataSource;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.IOManager;
import de.treona.shelfs.io.logger.LogLevel;
import net.dv8tion.jda.core.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLIOManager implements IOManager {

    private final MysqlDataSource dataSource;

    public MySQLIOManager(DatabaseCredentials databaseCredentials) {
        Shelfs.getLogger().logMessage("Starting the mysql io manager.", LogLevel.INFO);
        this.dataSource = new MysqlDataSource();
        this.dataSource.setUser(databaseCredentials.getUser());
        this.dataSource.setPassword(databaseCredentials.getPassword());
        this.dataSource.setServerName(databaseCredentials.getHost());
        this.dataSource.setPort(databaseCredentials.getPort());
        //this.dataSource.setURL("jdbc:mysql://" + databaseCredentials.getHost() + "/" + databaseCredentials.getDatabase() + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
        this.dataSource.setURL("jdbc:mysql://" + databaseCredentials.getHost() + "/" + databaseCredentials.getDatabase());
    }

    private boolean doesTableExist() {
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM permissions LIMIT 1;");
            preparedStatement.executeQuery();
            connection.close();
            preparedStatement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean canConnect() {
        try {
            Connection connection = this.dataSource.getConnection();
            connection.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void createPermissionTable() {
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE permissions(permissionId int PRIMARY KEY NOT NULL AUTO_INCREMENT, permissionName TEXT NOT NULL,userId LONG NOT NULL);");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = connection.prepareStatement("CREATE UNIQUE INDEX permissions_permissionId_uindex ON permissions (permissionId);");
            preparedStatement.executeUpdate();
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setupEnvironment() {
        if (!this.canConnect()) {
            Shelfs.getLogger().logMessage("Can't connect to the database.", LogLevel.WARNING);
            return false;
        }
        if (this.doesTableExist()) {
            Shelfs.getLogger().logMessage("Connected to the database.", LogLevel.INFO);
            return true;
        }
        this.createPermissionTable();
        Shelfs.getLogger().logMessage("Creating the shelfs tables.", LogLevel.INFO);
        if (!this.doesTableExist()) {
            Shelfs.getLogger().logMessage("No write permission on the database!", LogLevel.WARNING);
            return false;
        }
        Shelfs.getLogger().logMessage("Created the shelfs table successfully.", LogLevel.INFO);
        return true;
    }

    @Override
    public void addPermission(User user, String permission) {

    }

    @Override
    public void removePermission(User user, String permission) {

    }

    @Override
    public boolean hasPermission(User user, String permission) {
        return false;
    }

    @Override
    public List<String> getPermissions(User user) {
        return null;
    }

    @Override
    public String getPluginVar(String pluginName) {
        return null;
    }

    @Override
    public void setPluginVar(String pluginName, String pluginVar) {

    }
}
