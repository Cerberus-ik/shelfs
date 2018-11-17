package de.treona.leagueTools.commands

import de.treona.leagueTools.LeagueTools
import de.treona.leagueTools.account.LoadedDiscordSummoner
import de.treona.leagueTools.util.*
import de.treona.shelfs.api.message.DeleteMessage
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import no.stelar7.api.l4j8.basic.constants.types.GameQueueType
import no.stelar7.api.l4j8.impl.raw.MasteryAPI
import no.stelar7.api.l4j8.impl.raw.MatchAPI
import no.stelar7.api.l4j8.impl.raw.SpectatorAPI
import no.stelar7.api.l4j8.impl.raw.SummonerAPI
import no.stelar7.api.l4j8.pojo.match.Match
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors


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
        message?.let { author?.let { _ -> this.sendStatistics(it) } }
    }

    override fun execute(author: User?, message: Message?, textChannel: PrivateChannel?) {
        message?.let { author?.let { _ -> this.sendStatistics(it) } }
    }

    private fun sendStatistics(message: Message) {
        //TODO change this with 0.6.3
        val deleteMessage = DeleteMessage(MessageBuilder("Analysing games...").build(), message.channel)
        val loadedDiscordSummoner = SummonerUtil.getSummonerFromCommand(message) ?: return
        val embedBuilder = EmbedBuilder()
        val games = MatchAPI.getInstance().getMatchList(loadedDiscordSummoner.summoner.platform, loadedDiscordSummoner.summoner.accountId, null, null, 0, 50, null, null, null).stream().map { it.fullMatch }.collect(Collectors.toList())
        embedBuilder.setColor(Color.MAGENTA)
        embedBuilder.setTitle("Stats for: ${loadedDiscordSummoner.summoner.name} on ${RegionUtil.getPrettyRegionName(loadedDiscordSummoner.summoner.platform)}")
        embedBuilder.setThumbnail("https://ddragon.leagueoflegends.com/cdn/${LeagueTools.dataCacheManager.latestVersion}/img/profileicon/${loadedDiscordSummoner.summoner.profileIconId}.png")
        embedBuilder.addField("MMR", MMRUtil.getMMR(loadedDiscordSummoner.summoner).toString(), true)
        embedBuilder.addField(this.buildSoloQField(loadedDiscordSummoner))
        embedBuilder.addField(this.buildCurrentGameField(loadedDiscordSummoner))
        embedBuilder.addField(this.buildPremadeField(loadedDiscordSummoner, games))
        embedBuilder.addField(this.buildMasteryScoreField(loadedDiscordSummoner))
        embedBuilder.addField(this.buildMostPlayedChampionsField(loadedDiscordSummoner))
        embedBuilder.addField(this.buildWinRateField(loadedDiscordSummoner, games))
        deleteMessage.delete()
        message.channel.sendMessage(embedBuilder.build()).queue()
    }

    private fun buildSoloQField(loadedDiscordSummoner: LoadedDiscordSummoner): MessageEmbed.Field {
        val leagueEntry = loadedDiscordSummoner.summoner.leagueEntry.find { it.queueType == GameQueueType.RANKED_SOLO_5X5 }
        return when {
            leagueEntry == null -> MessageEmbed.Field("", "Unranked", true)
            leagueEntry.isInPromos -> {
                val stringBuilder = StringBuilder()
                for (i in 0..leagueEntry.miniSeries.target + 1) {
                    when {
                        leagueEntry.miniSeries.progress[i] == 'W' -> stringBuilder.append("✅")
                        leagueEntry.miniSeries.progress[i] == 'L' -> stringBuilder.append("❌")
                        else -> stringBuilder.append("⭕")
                    }
                }
                MessageEmbed.Field(leagueEntry.tierDivisionType.name, "Is in Promos ($stringBuilder)", true)
            }
            else -> MessageEmbed.Field(LeagueUtil.parseTierDivision(leagueEntry.tierDivisionType.name), "${leagueEntry.leaguePoints} lp", true)
        }
    }

    private fun buildMasteryScoreField(loadedDiscordSummoner: LoadedDiscordSummoner): MessageEmbed.Field {
        val score = MasteryAPI.getInstance().getMasteryScore(loadedDiscordSummoner.summoner.platform, loadedDiscordSummoner.summoner.summonerId)
        return MessageEmbed.Field("${loadedDiscordSummoner.summoner.name}'s total champion mastery score", "Score: $score", true)
    }

    private fun buildMostPlayedChampionsField(loadedDiscordSummoner: LoadedDiscordSummoner): MessageEmbed.Field {
        val topChamps = MasteryAPI.getInstance().getTopChampions(loadedDiscordSummoner.summoner.platform, loadedDiscordSummoner.summoner.summonerId, 5)
        val stringBuilder = StringBuilder()
        for (i in 0 until topChamps.size) {
            stringBuilder.append("${i + 1}.) ${LeagueTools.dataCacheManager.champions[topChamps[i].championId]?.name} ${topChamps[i].championPoints} points")
            stringBuilder.append(System.lineSeparator())
        }
        return MessageEmbed.Field("Most played champions", stringBuilder.toString(), false)
    }

    private fun buildWinRateField(loadedDiscordSummoner: LoadedDiscordSummoner, games: MutableList<Match>): MessageEmbed.Field {
        val wins = games.stream().filter{match: Match? ->
            val participant = match?.participants?.find { it.participantId == match.participantIdentities.find { participantIdentity -> participantIdentity.summonerId == loadedDiscordSummoner.summoner.summonerId }?.participantId }
            match?.didWin(participant)!!
        }.count()
        return MessageEmbed.Field("Win rate in the past ${games.size} games", (wins/games.size).toString(), false)
    }

    private fun buildPremadeField(loadedDiscordSummoner: LoadedDiscordSummoner, games: MutableList<Match>): MessageEmbed.Field {
        val potentialPremades = PremadeUtil.findPremadesFromMatchList(loadedDiscordSummoner.summoner.summonerId, games)
        if (potentialPremades.isEmpty()) {
            return MessageEmbed.Field("No team mates found in ${games.size}", "", false)
        }
        val stringBuilder = StringBuilder()
        potentialPremades.entries.sortedByDescending { (_, timesPlayedWith) -> timesPlayedWith }.take(10).forEach {
            val summoner = SummonerAPI.getInstance().getSummonerById(loadedDiscordSummoner.summoner.platform, it.key)
            stringBuilder.append("${summoner.name} played ${it.value} games out of ${games.size} ${System.lineSeparator()}")
        }
        return MessageEmbed.Field("Potential premades found: ${potentialPremades.size}", stringBuilder.toString(), false)
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