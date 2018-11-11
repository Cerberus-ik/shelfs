package de.treona.leagueTools.util

import no.stelar7.api.l4j8.basic.constants.types.GameQueueType
import no.stelar7.api.l4j8.basic.constants.types.TierDivisionType
import no.stelar7.api.l4j8.pojo.summoner.Summoner

class MMRUtil {

    companion object {
        fun getMMR(team: List<Summoner>): Int {
            var teamMMR = 0
            team.forEach { teamMMR += getMMR(it) }
            return teamMMR / team.size
        }

        fun getMMR(player: Summoner): Int {
            val leaguePosition = player.leagueEntry.find { it.queueType == GameQueueType.RANKED_SOLO_5X5 } ?: return -1
            var mmr = getDivisionMMR(leaguePosition.tierDivisionType)
            mmr += player.summonerLevel
            if(leaguePosition.isFreshBlood)
                mmr -= 20
            if(leaguePosition.isHotStreak)
                mmr += 30
            mmr += when {
                leaguePosition.tierDivisionType == TierDivisionType.CHALLENGER_I -> (leaguePosition.leaguePoints * 0.6).toInt()
                leaguePosition.tierDivisionType == TierDivisionType.MASTER_I -> (leaguePosition.leaguePoints * 0.8).toInt()
                else -> leaguePosition.leaguePoints
            }
            return mmr
        }

        private fun getDivisionMMR(tierDivisionType: TierDivisionType): Int {
            when(tierDivisionType){
                TierDivisionType.CHALLENGER_I -> return 4500
                TierDivisionType.MASTER_I -> return 4000
                TierDivisionType.DIAMOND_I -> return 3700
                TierDivisionType.DIAMOND_II -> return 3600
                TierDivisionType.DIAMOND_III -> return 3500
                TierDivisionType.DIAMOND_IV -> return 3400
                TierDivisionType.DIAMOND_V -> return 3300
                TierDivisionType.PLATINUM_I -> return 3150
                TierDivisionType.PLATINUM_II -> return 3050
                TierDivisionType.PLATINUM_III -> return 2950
                TierDivisionType.PLATINUM_IV -> return 2850
                TierDivisionType.PLATINUM_V -> return 2750
                TierDivisionType.GOLD_I -> return 2600
                TierDivisionType.GOLD_II -> return 2500
                TierDivisionType.GOLD_III -> return 2400
                TierDivisionType.GOLD_IV -> return 2300
                TierDivisionType.GOLD_V -> return 2200
                TierDivisionType.SILVER_I -> return 2050
                TierDivisionType.SILVER_II -> return 1950
                TierDivisionType.SILVER_III -> return 1850
                TierDivisionType.SILVER_IV -> return 1750
                TierDivisionType.SILVER_V -> return 1650
                TierDivisionType.BRONZE_I -> return 1500
                TierDivisionType.BRONZE_II -> return 1400
                TierDivisionType.BRONZE_III -> return 1300
                TierDivisionType.BRONZE_IV -> return 1200
                TierDivisionType.BRONZE_V -> return 1100
                TierDivisionType.UNRANKED -> return 1000
            }
        }

    }
}