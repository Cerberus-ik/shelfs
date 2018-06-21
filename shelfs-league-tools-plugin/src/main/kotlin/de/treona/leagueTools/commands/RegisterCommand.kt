package de.treona.leagueTools.commands

import de.treona.leagueTools.LeagueTools
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.entities.*

class RegisterCommand : GuildCommand, PrivateCommand {

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
        accountManager?.isRegistered(author?.idLong)
    }
}