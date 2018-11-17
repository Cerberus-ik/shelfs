package de.treona.leagueTools.commands

import de.treona.leagueTools.account.LoadedDiscordSummoner
import de.treona.leagueTools.util.SummonerUtil
import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.api.message.DeleteMessage
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.TextChannel
import no.stelar7.api.l4j8.impl.raw.MatchAPI
import no.stelar7.api.l4j8.pojo.match.ParticipantStats
import java.awt.Color
import java.util.concurrent.Callable
import java.util.stream.Collectors

class VisionCommand: GuildCommand {

    data class VisionData(val gamesAnalyzed: Int,
                          val averageVisionScore: Double,
                          val averageWardsPlaced: Double,
                          val averageWardsCleared: Double,
                          val favoriteTrinket: String)

    override fun getName(): String {
        return "Vision"
    }

    override fun getDescription(): String {
        return "Analise's your vision from your past games."
    }

    override fun getPermission(): Permission? {
        return null
    }

    override fun execute(author: Member?, message: Message?, textChannel: TextChannel?) {
        val loadedDiscordSummoner = SummonerUtil.getSummonerFromCommand(message!!)
        if(loadedDiscordSummoner == null){
            textChannel?.sendMessage("You have to be registered or mention a player to see their stats.")?.queue()
            return
        }
        val callable = Callable { return@Callable this.analyzeGames(loadedDiscordSummoner) }
        DeleteMessage("Analysing games...", textChannel, callable)
        val data = callable.call()
        textChannel!!.sendMessage(this.buildVisionMessage(loadedDiscordSummoner, data))
    }

    private fun buildVisionMessage(loadedDiscordSummoner: LoadedDiscordSummoner, data: VisionData): MessageEmbed{
        val embedBuilder = EmbedBuilder()
        embedBuilder.setColor(Color.GREEN)
        embedBuilder.setTitle("Vision of: " + loadedDiscordSummoner.summoner.name)
        embedBuilder.addField("Average vision", data.averageVisionScore.toString(), true)
        embedBuilder.addField("Average wards placed", data.averageWardsPlaced.toString(), true)
        embedBuilder.addField("Average wards cleared", data.averageWardsCleared.toString(), true)
        embedBuilder.addField("Favorite trinket", data.favoriteTrinket, true)
        embedBuilder.setFooter("${data.gamesAnalyzed} games analyzed.", Shelfs.getJda().selfUser.avatarUrl)
        return embedBuilder.build()
    }

    private fun analyzeGames(loadedDiscordSummoner: LoadedDiscordSummoner): VisionData{
        val games = MatchAPI.getInstance().getMatchList(loadedDiscordSummoner.summoner.platform, loadedDiscordSummoner.summoner.accountId, null, null, 0, 50, null, null, null).stream().map { it.fullMatch }.collect(Collectors.toList())
        var averageVisionScore = 0.0
        var averageWardsCleared = 0.0
        var averageWardsPlaced = 0.0
        val mostUsedTrinket = mutableMapOf<Long, Int>()

        games.forEach { game ->
            val participant = game.getParticipantFromSummonerId(loadedDiscordSummoner.summoner.summonerId)
            averageVisionScore += participant?.stats?.visionScore!!
            averageWardsPlaced += participant.stats?.wardsPlaced!!
            averageWardsCleared += participant.stats?.wardsKilled!!
            //3340 green
            //3363 blue
            //3364 red
            val times = mostUsedTrinket.getOrDefault(this.getTrinket(participant.stats), 0)
            mostUsedTrinket[this.getTrinket(participant.stats)] = times + 1
        }
        var trinketName = String()
        when {
            mostUsedTrinket.maxBy { it.value }!!.value == 3340 -> trinketName = "Warding Totem"
            mostUsedTrinket.maxBy { it.value }!!.value == 3363 -> trinketName = "Farsight Alteration"
            mostUsedTrinket.maxBy { it.value }!!.value == 3363 -> trinketName = "Oracle Lens"
        }
        return VisionData(games.size, averageVisionScore, averageWardsPlaced, averageWardsCleared, trinketName)
    }

    private fun getTrinket(stats: ParticipantStats): Long{
        var item = stats.item0
        if(item == 3340L || item == 3363L || item == 3364L)
            return item
        item = stats.item1
        if(item == 3340L || item == 3363L || item == 3364L)
            return item
        item = stats.item2
        if(item == 3340L || item == 3363L || item == 3364L)
            return item
        item = stats.item3
        if(item == 3340L || item == 3363L || item == 3364L)
            return item
        item = stats.item4
        if(item == 3340L || item == 3363L || item == 3364L)
            return item
        item = stats.item5
        if(item == 3340L || item == 3363L || item == 3364L)
            return item
        item = stats.item6
        if(item == 3340L || item == 3363L || item == 3364L)
            return item
        return -1
    }
}