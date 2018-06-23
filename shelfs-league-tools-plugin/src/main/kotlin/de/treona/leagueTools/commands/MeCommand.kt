package de.treona.leagueTools.commands

import de.treona.leagueTools.LeagueTools
import de.treona.leagueTools.account.LoadedDiscordSummoner
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
        return "Confirm"
    }

    override fun getDescription(): String {
        return "Let's you confirm the registration after you set the verification code."
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
            val loadedDiscordSummoner = LeagueTools.accountManager.getDiscordSummoner(author.idLong).upgrade()
            message.channel.sendMessage(this.buildMessage(loadedDiscordSummoner)).queue()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

    }

    private fun buildMessage(loadedDiscordSummoner: LoadedDiscordSummoner): MessageEmbed {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setTitle("Stats for ${loadedDiscordSummoner.summoner.name}")
        embedBuilder.setColor(Color.MAGENTA)
        embedBuilder.setThumbnail("http://ddragon.leagueoflegends.com/cdn/6.24.1/img/profileicon/${loadedDiscordSummoner.summoner.profileIconId}.png")
        embedBuilder.addField("Level", "Level: ${loadedDiscordSummoner.summoner.summonerLevel}", true)
        embedBuilder.addField(this.buildMostPlayedChampionField(loadedDiscordSummoner.summoner))
        //embedBuilder.addField("Most played champion", "${loadedDiscordSummoner.summoner.championMasteries.get(0).}", true)
        return embedBuilder.build()
    }

    private fun buildMostPlayedChampionField(summoner: Summoner): MessageEmbed.Field {
        val championMastery = summoner.championMasteries[0]
        return MessageEmbed.Field("Most played champion", "Champion: ${LeagueTools.dataCacheManager.champions[championMastery.championId]?.name} ${System.lineSeparator()}Points: ${championMastery.championPoints}", true)
    }
}