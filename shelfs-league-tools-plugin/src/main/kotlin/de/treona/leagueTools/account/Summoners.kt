package de.treona.leagueTools.account

import net.dv8tion.jda.core.entities.User
import no.stelar7.api.l4j8.basic.constants.api.Platform
import no.stelar7.api.l4j8.pojo.summoner.Summoner

data class DiscordSummoner(val user: User, val summonerId: Long, val region: Platform)

data class LoadedDiscordSummoner(val user: User, var summoner: Summoner, var lastUpdate: Long = System.nanoTime()) {
    fun update() {
        summoner = Summoner.bySummonerId(summoner.summonerId, summoner.platform)
        lastUpdate = System.nanoTime()
    }
}