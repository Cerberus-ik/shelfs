package de.treona.leagueTools.io

import com.mysql.cj.jdbc.MysqlDataSource
import de.treona.leagueTools.account.DiscordSummoner
import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.io.database.DatabaseCredentials
import de.treona.shelfs.io.logger.Logger
import net.dv8tion.jda.core.entities.User
import no.stelar7.api.l4j8.basic.constants.api.Platform
import java.sql.SQLException

class DatabaseManager(private val logger: Logger) {

    private val dataSource = MysqlDataSource()

    fun setup(databaseCredentials: DatabaseCredentials) {
        this.logger.logMessage("Starting the mysql manager.", de.treona.shelfs.io.logger.LogLevel.INFO)
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
        this.setupEnvironment()
    }

    private fun doesSummonerTableExist(): Boolean {
        return try {
            val connection = this.dataSource.connection
            val preparedStatement = connection.prepareStatement("SELECT 1 FROM summoners LIMIT 1;")
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
            e.printStackTrace()
            false
        }
    }

    private fun createSummonersTable() {
        try {
            val connection = this.dataSource.connection
            var preparedStatement = connection.prepareStatement("CREATE TABLE summoners( userId int PRIMARY KEY NOT NULL AUTO_INCREMENT, summonerId LONG NOT NULL, region TEXT NOT NULL, discordId LONG);")
            preparedStatement.executeUpdate()
            preparedStatement.close()
            preparedStatement = connection.prepareStatement("CREATE UNIQUE INDEX summoners_userId_uindex ON summoners (userId);")
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

    fun isDiscordUserRegistered(discordId: Long): Boolean {
        dataSource.connection.prepareStatement("SELECT 1 discordId FROM summoners WHERE discordId = ?;").use {
            it.setLong(1, discordId)
            it.executeQuery().use { return it.next() }
        }
    }

    fun isSummonerRegistered(summonerId: Long, platform: Platform): Boolean {
        dataSource.connection.prepareStatement("SELECT summonerId FROM summoners WHERE (summonerId = ? AND region = ?);").use {
            it.setLong(1, summonerId)
            it.setString(2, platform.name)
            it.executeQuery().use { return it.next() }
        }
    }

    fun getDiscordUserBySummonerId(summonerId: Long, platform: Platform): User? {
        dataSource.connection.prepareStatement("SELECT discordId FROM summoners WHERE (summonerId = ? AND region = ?);").use {
            it.setLong(1, summonerId)
            it.setString(1, platform.name)
            it.executeQuery().use {
                return if (!it.next())
                    null
                else
                    Shelfs.getJda().getUserById(it.getLong(1))
            }
        }
    }

    fun getDiscordSummonerByDiscordId(discordId: Long): DiscordSummoner? {
        dataSource.connection.prepareStatement("SELECT (summonerId, region) FROM summoners WHERE discordId = ?;").use {
            it.setLong(1, discordId)
            it.executeQuery().use {
                return if (!it.next())
                    null
                else
                    DiscordSummoner(Shelfs.getJda().getUserById(discordId), it.getLong(1), Platform.getFromCode(it.getString(2)).get())
            }
        }
    }

    fun registerSummoner(discordSummoner: DiscordSummoner) {
        dataSource.connection.prepareStatement("INSERT INTO summoners (summonerId, region, discordId) VALUES (?, ?, ?);").use {
            it.setLong(1, discordSummoner.summonerId)
            it.setString(2, discordSummoner.region.name)
            it.setLong(3, discordSummoner.user.idLong)
            it.executeUpdate()
        }
    }
}