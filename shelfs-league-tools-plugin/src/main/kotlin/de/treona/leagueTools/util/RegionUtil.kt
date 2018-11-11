package de.treona.leagueTools.util

import de.treona.leagueTools.LeagueTools
import net.dv8tion.jda.core.entities.Message
import no.stelar7.api.l4j8.basic.constants.api.Platform

class RegionUtil {

    companion object {
        val regions = HashMap<Platform, MutableList<String>>()

        fun getRegionByCommand(message: Message): Platform {
            val args = message.contentRaw.split(" ")
            if(args.size > 1){
                return getRegionByName(args[1])
            }else{
                val discordSummoner = LeagueTools.accountManager.getDiscordSummonerByDiscordId(message.author.idLong)
                if (discordSummoner == null) {
                    message.channel.sendMessage("You didn't specify a region, ``EUW`` is selected by default.").queue()
                    return Platform.EUW1
                }
                return discordSummoner.region
            }
        }

        fun getRegionByName(name: String): Platform {
            var result = Platform.UNKNOWN
            regions.forEach { platform, names ->
                if (names.contains(name.toUpperCase())) {
                    result = platform
                    return@forEach
                }
            }
            return result
        }

        fun getPrettyRegionName(region: Platform): String {
            return when (region) {
                Platform.LA1 -> "Lan"
                Platform.LA2 -> "Las"
                else -> {
                    val regionName = Regex("[^A-Z]").replace(region.name, "")
                    regionName.substring(0, 1) + regionName.substring(1, regionName.length).toLowerCase()
                }
            }
        }
    }

    init {
        Platform.values().forEach { regions[it] = arrayListOf(it.name) }
        regions[Platform.EUW1]?.add("EUW")
        regions[Platform.EUN1]?.add("EUN")
        regions[Platform.EUN1]?.add("EUNE")
        regions[Platform.NA1]?.add("NA")
    }
}