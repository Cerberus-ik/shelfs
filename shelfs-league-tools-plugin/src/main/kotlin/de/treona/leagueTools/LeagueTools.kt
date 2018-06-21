package de.treona.leagueTools

import de.treona.leagueTools.account.AccountManager
import de.treona.leagueTools.commands.RegisterCommand
import de.treona.leagueTools.io.ConfigReader
import de.treona.leagueTools.io.DatabaseManager
import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.api.plugin.ShelfsPlugin
import de.treona.shelfs.io.logger.Logger

class LeagueTools : ShelfsPlugin() {

    override fun onEnable() {
        if (config == null || config == defaultConfig)
            super.writeDefaultConfig()
        accountManager = AccountManager(Logger("Account-Manager"))
        databaseManager = DatabaseManager(Logger("Database-Manager"), ConfigReader.getDatabaseCredentials(super.defaultConfig.toString()))

        Shelfs.getCommandManager().registerCommand(this, "register", RegisterCommand())
    }

    companion object {
        var accountManager: AccountManager? = null
        var databaseManager: DatabaseManager? = null
    }
}