package de.treona.leagueTools.commands

import de.treona.leagueTools.LeagueTools
import de.treona.leagueTools.account.LoadedDiscordSummoner
import de.treona.leagueTools.util.LeagueUtil
import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.*
import no.stelar7.api.l4j8.pojo.summoner.Summoner
import java.awt.Color

class MeCommand : GuildCommand, PrivateCommand {


    override fun getPermission(): Permission? {
        return null
    }

    override fun getName(): String {
        return "Me"
    }

    override fun getDescription(): String {
        return "Shows you the basic information about a summoner"
    }

    override fun execute(author: Member?, message: Message?, textChannel: TextChannel?) {
        this.execute(author?.user, message)
    }

    override fun execute(author: User?, message: Message?, textChannel: PrivateChannel?) {
        this.execute(author, message)
    }

    private fun execute(author: User?, message: Message?) {
        if (message == null || author == null) {
            return
        }
        if (!LeagueTools.accountManager.isUserRegistered(author.idLong)) {
            message.channel.sendMessage("You firstly need to link your discord and your league account with ``${Shelfs.getCommandManager().commandPrefix}register <region> <summoner name>").queue()
            return
        }
        try {
            val loadedDiscordSummoner = LeagueTools.accountManager.getDiscordSummonerByDiscordId(author.idLong)?.upgrade()
                    ?: return
            message.channel.sendMessage(this.buildMessage(loadedDiscordSummoner)).queue()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun buildMessage(loadedDiscordSummoner: LoadedDiscordSummoner): MessageEmbed {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setTitle("Stats for ${loadedDiscordSummoner.summoner.name}")
        embedBuilder.setColor(Color.MAGENTA)
        embedBuilder.setThumbnail("https://ddragon.leagueoflegends.com/cdn/${LeagueTools.dataCacheManager.latestVersion}/img/profileicon/${loadedDiscordSummoner.summoner.profileIconId}.png")
        embedBuilder.addField("Level", "Level: ${loadedDiscordSummoner.summoner.summonerLevel}", true)
        this.buildRankedFields(loadedDiscordSummoner.summoner).forEach { embedBuilder.addField(it) }
        embedBuilder.addField(this.buildMostPlayedChampionField(loadedDiscordSummoner.summoner))
        return embedBuilder.build()
    }

    private fun buildMostPlayedChampionField(summoner: Summoner): MessageEmbed.Field {
        val championMastery = summoner.championMasteries[0]
        return MessageEmbed.Field("Most played champion", "Champion: ${LeagueTools.dataCacheManager.champions[championMastery.championId]?.name} ${System.lineSeparator()}Points: ${championMastery.championPoints}", true)
    }

    private fun buildRankedFields(summoner: Summoner): List<MessageEmbed.Field> {
        val leagueEntries = summoner.leagueEntry
        val fields = arrayListOf<MessageEmbed.Field>()
        leagueEntries.forEach {
            fields.add(MessageEmbed.Field(it.queueType.name, "Rank: ${LeagueUtil.parseTierDivision(it.tierDivisionType.name)} (${it.leaguePoints}lp)", true))
        }
        return fields
    }
}