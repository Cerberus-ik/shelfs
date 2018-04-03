package de.treona.shelfs.io.database;

import com.mysql.cj.jdbc.MysqlDataSource;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.IOManager;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.permission.Permission;
import de.treona.shelfs.permission.StringPermission;
import net.dv8tion.jda.core.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        try {
            this.dataSource.setServerTimezone("UTC");
            //TODO add time zone support
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public void addPermission(User user, Permission permission) {
        if (!(permission instanceof StringPermission)) {
            throw new IllegalArgumentException("Only string permissions are supported here.");
        }
        StringPermission stringPermission = (StringPermission) permission;
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO permissions (permissionName, userId) VALUES (?, ?);");
            preparedStatement.setString(1, stringPermission.getPermission());
            preparedStatement.setString(2, user.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removePermission(User user, String permission) {
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM permissions WHERE permissionName = ? AND userId = ? LIMIT 1");
            preparedStatement.setString(1, permission);
            preparedStatement.setString(2, user.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasPermission(User user, String permission) {
        boolean returnValue = false;
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT permissionName FROM permissions WHERE userId = ? AND permissionName = ?;");
            preparedStatement.setString(1, user.getId());
            preparedStatement.setString(2, permission);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                returnValue = true;
            resultSet.close();
            preparedStatement.close();
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    @Override
    public List<StringPermission> getPermissions(User user) {
        List<StringPermission> permissions = new ArrayList<>();
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT permissionName FROM permissions WHERE userId = ?;");
            preparedStatement.setString(1, user.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
                permissions.add(new StringPermission(resultSet.getString(1)));
            resultSet.close();
            preparedStatement.close();
            connection.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    @Override
    public String getPluginVar(String pluginName) {
        return null;
    }

    @Override
    public void setPluginVar(String pluginName, String pluginVar) {

    }
}
