package de.treona.leagueTools.commands

import de.treona.leagueTools.LeagueTools
import de.treona.leagueTools.account.DiscordSummoner
import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.io.logger.LogLevel
import de.treona.shelfs.io.logger.Logger
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.entities.*
import no.stelar7.api.l4j8.basic.constants.api.Platform
import no.stelar7.api.l4j8.pojo.summoner.Summoner

class RegisterCommand : GuildCommand, PrivateCommand {

    private val logger = Logger("Register-Command")

    override fun getPermission(): Permission? {
        return null
    }

    override fun getName(): String {
        return "Register"
    }

    override fun getDescription(): String {
        return "Let's you link your league account with your discord account"
    }

    override fun execute(author: Member?, message: Message?, textChannel: TextChannel?) {
        this.execute(author?.user, message)
    }

    override fun execute(author: User?, message: Message?, textChannel: PrivateChannel?) {
        this.execute(author, message)
    }

    private fun execute(author: User?, message: Message?) {
        val accountManager = LeagueTools.accountManager
        if (message == null) {
            this.logger.logMessage("Register message is null!", LogLevel.ERROR)
            return
        }
        if (accountManager.isUserRegistered(author?.idLong)) {
            message.channel?.sendMessage("You are already registered!")?.queue()
            return
        }
        val args = message.contentRaw.split(" ")
        if (args.size < 2) {
            message.channel.sendMessage("Use: ${Shelfs.getCommandManager().commandPrefix}register ``region`` ``Summoner Name``").queue()
        }
        val region = Platform.getFromCode(args[1])
        if (region == null || !region.isPresent) {
            message.channel.sendMessage("``" + args[1] + "`` is an unknown region.").queue()
            return
        }
        val stringBuilder = StringBuilder()
        args.subList(2, args.size).forEach { stringBuilder.append(it).append(" ") }
        val summonerName = stringBuilder.toString().substring(0, stringBuilder.toString().length)
        if (!LeagueTools.accountManager.doesSummonerExist(summonerName, region.get())) {
            message.channel.sendMessage("Could not find the summoner: ``$summonerName``")
            return
        }
        val summoner = Summoner.byName(summonerName, region.get())
        if (summoner == null) {
            message.channel.sendMessage("Could not find ``$summonerName``").queue()
            return
        }
        if (LeagueTools.accountManager.isSummonerRegistered(summoner.summonerId, summoner.platform)) {
            message.channel.sendMessage("This league account is already connected to a discord account.").queue()
            return
        }
        author?.let { this.register(it, summoner, message) }
    }

    private fun register(user: User, summoner: Summoner, message: Message) {
        val code = LeagueTools.registrationManager.startRegistering(DiscordSummoner(user, summoner.summonerId, summoner.platform))
        message.channel.sendMessage("Click on the ⚙ icon in your launcher and scroll down to the ``VERIFICATION`` tab." +
                System.lineSeparator() + "Enter the code: `` $code `` in your client and use: ``${Shelfs.getCommandManager().commandPrefix}confirm`` to finish your registration.").queue()

    }
}