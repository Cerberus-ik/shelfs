package de.treona.leagueTools.commands

import de.treona.leagueTools.LeagueTools
import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.io.logger.LogLevel
import de.treona.shelfs.io.logger.Logger
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.entities.*

class ConfirmCommand : GuildCommand, PrivateCommand {

    private val logger = Logger("Confirm-Command")

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
            this.logger.logMessage("Register message and/or author is null!", LogLevel.ERROR)
            return
        }
        if (!LeagueTools.registrationManager.isInRegistration(author)) {
            message.channel.sendMessage("You firstly need to generate your verification code with ``${Shelfs.getCommandManager().commandPrefix}register <region> <summoner name>").queue()
            return
        }
        if (!LeagueTools.registrationManager.valid(author)) {
            message.channel.sendMessage("Your verification code doesn't match. Try again in a few moments.").queue()
            return
        }
        LeagueTools.registrationManager.register(author)
        message.channel.sendMessage("Registration was successful.").queue()
    }


}