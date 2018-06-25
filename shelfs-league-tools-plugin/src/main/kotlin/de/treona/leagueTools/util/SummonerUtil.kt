package de.treona.leagueTools.util

import de.treona.leagueTools.LeagueTools
import de.treona.leagueTools.account.LoadedDiscordSummoner
import net.dv8tion.jda.core.entities.Message
import no.stelar7.api.l4j8.basic.constants.api.Platform
import no.stelar7.api.l4j8.impl.raw.SummonerAPI

class SummonerUtil {

    companion object {
        fun getSummonerFromCommand(message: Message): LoadedDiscordSummoner? {
            val args = message.contentRaw.split(" ")
            val user = message.author
            if (args.size < 2) {
                val discordSummoner = LeagueTools.accountManager.getDiscordSummonerByDiscordId(user.idLong)
                if (discordSummoner == null) {
                    message.channel.sendMessage("You didn't specify a player and are not registered yourself.").queue()
                    return null
                }
                return discordSummoner.upgrade()
            }
            val mentions = message.mentionedMembers
            if (mentions.size > 1) {
                message.channel.sendMessage("Please mention only one user").queue()
                return null
            } else if (mentions.size == 1) {
                val discordSummoner = LeagueTools.accountManager.getDiscordSummonerByDiscordId(mentions[0].user.idLong)
                if (discordSummoner == null) {
                    message.channel.sendMessage("You didn't specify a player and are not registered yourself.").queue()
                    return null
                }
                return discordSummoner.upgrade()
            }
            var region = RegionUtil.getRegionByName(args[1])
            var regionIncluded = 1
            if (region == Platform.UNKNOWN) {
                val commandAuthor = LeagueTools.accountManager.getDiscordSummonerByDiscordId(user.idLong)
                if (commandAuthor == null) {
                    message.channel.sendMessage("You didn't specify a region and are not registered yourself (default region).").queue()
                    return null
                }
                region = commandAuthor.region
                regionIncluded = 0
            }
            val summonerName = parseSummonerName(args, regionIncluded)
            if (!LeagueTools.accountManager.doesSummonerExist(summonerName, region)) {
                message.channel.sendMessage("The summoner ``$summonerName`` does not exist on ``${RegionUtil.getPrettyRegionName(region)}``").queue()
                return null
            }
            return LoadedDiscordSummoner(user, SummonerAPI.getInstance().getSummonerByName(region, summonerName))
        }

        private fun parseSummonerName(args: List<String>, regionIncluded: Int): String {
            val stringBuilder = StringBuilder()
            for (i in 1 + regionIncluded until args.size) {
                stringBuilder.append(args[i]).append(" ")
            }
            var summonerName = stringBuilder.toString()
            summonerName = summonerName.substring(0, summonerName.length)
            return summonerName
        }
    }
}