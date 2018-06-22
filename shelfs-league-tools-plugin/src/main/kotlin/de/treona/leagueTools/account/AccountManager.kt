package de.treona.leagueTools.account

import de.treona.leagueTools.LeagueTools
import de.treona.shelfs.io.logger.Logger
import no.stelar7.api.l4j8.basic.constants.api.Platform
import no.stelar7.api.l4j8.pojo.summoner.Summoner

class AccountManager(var logger: Logger) {

    fun isUserRegistered(discordId: Long?): Boolean {
        if (discordId == null)
            return false
        return LeagueTools.databaseManager.isDiscordUserRegistered(discordId)
    }

    fun isSummonerRegistered(summonerId: Long?, region: Platform?): Boolean {
        if (summonerId == null || region == null)
            return false
        return LeagueTools.databaseManager.isSummonerRegistered(summonerId, region)
    }

    fun doesSummonerExist(summonerName: String, region: Platform): Boolean {
        return Summoner.byName(summonerName, region) != null
    }

    fun upgrade(discordSummoner: DiscordSummoner): LoadedDiscordSummoner {
        return LoadedDiscordSummoner(discordSummoner.user, Summoner.bySummonerId(discordSummoner.summonerId, discordSummoner.region))
    }
}