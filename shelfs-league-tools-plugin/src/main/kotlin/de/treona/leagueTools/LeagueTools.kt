package de.treona.leagueTools

import de.treona.leagueTools.account.AccountManager
import de.treona.leagueTools.account.RegistrationManager
import de.treona.leagueTools.commands.*
import de.treona.leagueTools.data.DataCacheManager
import de.treona.leagueTools.io.ConfigReader
import de.treona.leagueTools.io.DatabaseManager
import de.treona.leagueTools.util.RegionUtil
import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.api.plugin.ShelfsPlugin
import de.treona.shelfs.io.logger.Logger
import no.stelar7.api.l4j8.basic.APICredentials
import no.stelar7.api.l4j8.impl.L4J8


class LeagueTools : ShelfsPlugin() {

    override fun onEnable() {
        if (config == null || config == defaultConfig)
            super.writeDefaultConfig()
        val api = L4J8(APICredentials(super.getConfig().getString("api-key"), super.getConfig().getString("tournament-key")))

        RegionUtil()
        databaseManager.setup(ConfigReader.getDatabaseCredentials(super.getConfig().toString()))
        dataCacheManager.start()
        Shelfs.getCommandManager().registerCommand(this, "register", RegisterCommand())
        Shelfs.getCommandManager().registerCommand(this, "confirm", ConfirmCommand())
        Shelfs.getCommandManager().registerCommand(this, "regions", RegionsCommand())
        Shelfs.getCommandManager().registerCommand(this, "me", MeCommand(), "league")
        Shelfs.getCommandManager().registerCommand(this, "status", StatusCommand())
        Shelfs.getCommandManager().registerCommand(this, "statistics", StatisticsCommand(), "stats")
        Shelfs.getCommandManager().registerCommand(this, "vision", VisionCommand())
    }

    companion object {
        val accountManager = AccountManager(Logger("Account-Manager"))
        val databaseManager = DatabaseManager(Logger("Database-Manager"))
        val registrationManager = RegistrationManager()
        val dataCacheManager = DataCacheManager()
    }
}