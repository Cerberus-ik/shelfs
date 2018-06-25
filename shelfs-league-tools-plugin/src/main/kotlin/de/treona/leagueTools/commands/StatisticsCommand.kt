package de.treona.leagueTools.commands

import de.treona.leagueTools.LeagueTools
import de.treona.leagueTools.account.LoadedDiscordSummoner
import de.treona.leagueTools.util.RegionUtil
import de.treona.leagueTools.util.SummonerUtil
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.*
import no.stelar7.api.l4j8.basic.constants.types.GameQueueType
import no.stelar7.api.l4j8.impl.raw.SpectatorAPI
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*


class StatisticsCommand : GuildCommand, PrivateCommand {

    override fun getName(): String {
        return "Statistics"
    }

    override fun getDescription(): String {
        return "Shows you statistics about a player"
    }

    override fun getPermission(): Permission? {
        return null
    }

    override fun execute(author: Member?, message: Message?, textChannel: TextChannel?) {
        message?.let { author?.let { it1 -> this.sendStatistics(it1.user, it) } }
    }

    override fun execute(author: User?, message: Message?, textChannel: PrivateChannel?) {
        message?.let { author?.let { it1 -> this.sendStatistics(it1, it) } }
    }

    private fun sendStatistics(user: User, message: Message) {
        val loadedDiscordSummoner = SummonerUtil.getSummonerFromCommand(message) ?: return
        //message.channel.sendMessage("Summoner: ${loadedDiscordSummoner.summoner.name} on ${RegionUtil.getPrettyRegionName(loadedDiscordSummoner.summoner.platform)}").queue()
        val embedBuilder = EmbedBuilder()
        embedBuilder.setColor(Color.MAGENTA)
        embedBuilder.setTitle("Stats for: ${loadedDiscordSummoner.summoner.name} on ${RegionUtil.getPrettyRegionName(loadedDiscordSummoner.summoner.platform)}")
        embedBuilder.setThumbnail("https://ddragon.leagueoflegends.com/cdn/${LeagueTools.dataCacheManager.latestVersion}/img/profileicon/${loadedDiscordSummoner.summoner.profileIconId}.png")

        embedBuilder.addField(this.buildSoloQField(loadedDiscordSummoner))
        embedBuilder.addField(this.buildCurrentGameField(loadedDiscordSummoner))
        message.channel.sendMessage(embedBuilder.build()).queue()
    }

    private fun buildSoloQField(loadedDiscordSummoner: LoadedDiscordSummoner): MessageEmbed.Field {
        val leagueEntry = loadedDiscordSummoner.summoner.leagueEntry.find { it.queueType == GameQueueType.RANKED_SOLO_5X5 }
        return if (leagueEntry == null)
            MessageEmbed.Field("", "Unranked", true)
        else if (leagueEntry.isInPromos) {
            val stringBuilder = StringBuilder()
            for (i in 0..leagueEntry.miniSeries.target + 1) {
                when {
                    leagueEntry.miniSeries.progress[i] == 'W' -> stringBuilder.append("✅")
                    leagueEntry.miniSeries.progress[i] == 'L' -> stringBuilder.append("❌")
                    else -> stringBuilder.append("⭕")
                }
            }
            MessageEmbed.Field(leagueEntry.tierDivisionType.name, "Is in Promos ($stringBuilder)", true)
        } else {
            MessageEmbed.Field(leagueEntry.tierDivisionType.name, "${leagueEntry.leaguePoints} lp", true)
        }

    }

    private fun buildCurrentGameField(loadedDiscordSummoner: LoadedDiscordSummoner): MessageEmbed.Field {
        val game = SpectatorAPI.getInstance().getCurrentGame(loadedDiscordSummoner.summoner.platform, loadedDiscordSummoner.summoner.summonerId)
                ?: return MessageEmbed.Field("**Spectating**", "Player is not in a game right now", true)
        return MessageEmbed.Field("Playing a ${game.gameType.name} game as " +
                "${LeagueTools
                        .dataCacheManager
                        .champions[game.participants
                        .find { it.summonerId == loadedDiscordSummoner.summoner.summonerId }
                        ?.championId]?.name}",
                this.getDateFromMillis(game.gameLength * 1000),
                true)
    }

    private fun getDateFromMillis(millis: Long): String {
        val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        return formatter.format(Date(millis))
    }
}