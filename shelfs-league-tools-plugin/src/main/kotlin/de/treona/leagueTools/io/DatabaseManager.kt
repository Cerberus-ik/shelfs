package de.treona.leagueTools.io

import com.mysql.cj.jdbc.MysqlDataSource
import de.treona.shelfs.io.logger.Logger
import java.sql.SQLException

class DatabaseManager(private val logger: Logger, databaseCredentials: DatabaseCredentialsImplementation) {

    private val dataSource = MysqlDataSource()

    init {
        this.logger.logMessage("Starting the mysql manager.", de.treona.shelfs.io.logger.LogLevel.INFO)
        this.setupEnvironment()
        this.dataSource.user = databaseCredentials.user
        this.dataSource.setPassword(databaseCredentials.password)
        this.dataSource.serverName = databaseCredentials.host
        this.dataSource.port = databaseCredentials.port
        try {
            this.dataSource.useSSL = true
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        try {
            this.dataSource.serverTimezone = "UTC"
            //TODO add time zone support
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        this.dataSource.setURL("jdbc:mysql://" + databaseCredentials.host + "/" + databaseCredentials.database)
    }

    private fun doesSummonerTableExist(): Boolean {
        return try {
            val connection = this.dataSource.connection
            val preparedStatement = connection.prepareStatement("SELECT 1 FROM permissions LIMIT 1;")
            preparedStatement.executeQuery()
            connection.close()
            preparedStatement.close()
            true
        } catch (e: SQLException) {
            false
        }
    }

    private fun canConnect(): Boolean {
        return try {
            val connection = this.dataSource.connection
            connection.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createSummonersTable() {
        try {
            val connection = this.dataSource.connection
            var preparedStatement = connection.prepareStatement("CREATE TABLE summoners(summonerId long PRIMARY KEY NOT NULL, region TEXT NOT NULL);")
            preparedStatement.executeUpdate()
            preparedStatement.close()
            preparedStatement = connection.prepareStatement("CREATE UNIQUE INDEX summoners_summonerId_uindex ON summoners (summonerId);")
            preparedStatement.executeUpdate()
            connection.close()
            preparedStatement.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun setupEnvironment(): Boolean {
        if (!this.canConnect()) {
            this.logger.logMessage("Can't connect to the database.", de.treona.shelfs.io.logger.LogLevel.WARNING)
            return false
        }
        if (this.doesSummonerTableExist()) {
            this.logger.logMessage("Connected to the database.", de.treona.shelfs.io.logger.LogLevel.INFO)
            return true
        } else {
            this.createSummonersTable()
            this.logger.logMessage("Creating the summoners table.", de.treona.shelfs.io.logger.LogLevel.INFO)
        }
        if (!this.doesSummonerTableExist()) {
            this.logger.logMessage("No write permission on the database!", de.treona.shelfs.io.logger.LogLevel.WARNING)
            return false
        }

        this.logger.logMessage("All tables got created.", de.treona.shelfs.io.logger.LogLevel.INFO)
        return true
    }
}