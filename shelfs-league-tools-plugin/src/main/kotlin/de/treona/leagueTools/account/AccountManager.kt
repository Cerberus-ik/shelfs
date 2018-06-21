package de.treona.leagueTools.account

import de.treona.shelfs.io.logger.Logger

class AccountManager(private var logger: Logger) {

    fun isRegistered(discordId: Long?): Boolean {
        if (discordId == null)
            return false

        return true
    }
}