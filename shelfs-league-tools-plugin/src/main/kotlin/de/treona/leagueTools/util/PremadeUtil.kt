package de.treona.leagueTools.util

import no.stelar7.api.l4j8.pojo.match.Match

class PremadeUtil {

    companion object {
        fun findPremadesFromMatchList(summonerId: Long, games: List<Match>): Map<Long, Int> {
            val teamMates = HashMap<Long, Int>()
            games.forEach { match ->
                val team = match.getParticipantFromSummonerId(summonerId)
                match.participants.stream().filter {
                    it.team == team &&
                            it.participantId != match.getParticipantFromSummonerId(summonerId).participantId
                }.forEach { participant ->
                    val teamMateSummonerId = match.participantIdentities.find { it.participantId == participant.participantId }?.summonerId
                    teamMates[teamMateSummonerId!!] = teamMates.getOrDefault(teamMateSummonerId, 0) + 1
                }
            }
            return teamMates.filterValues { it > 1 }
        }
    }
}