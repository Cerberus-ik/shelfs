package de.treona.leagueTools.util

import no.stelar7.api.l4j8.pojo.match.Match

class PremadeUtil {

    companion object {
        fun findPremadesFromMatchList(summonerId: Long, games: List<Match>): Map<Long, Int> {
//            val teamMates = HashMap<Long, Int>()
//            games.forEach {
//                it.participants.stream().filter {
//                    it.team == it.participantId().e.team
//                            && it.participantId != match.getParticipantFromSummonerId(summonerId).participantId
//                }.forEach {
//                    val participant = it
//                    val teamMateSummonerId = it.participantIdentities.find { it.participantId == participant.participantId }?.summonerId
//                            ?: return@forEach
//                    teamMates[teamMateSummonerId] = teamMates.getOrDefault(teamMateSummonerId, 0) + 1
//                }
//            }
//            return teamMates.filterValues { it > 1 }
            return mutableMapOf()
        }
    }
}