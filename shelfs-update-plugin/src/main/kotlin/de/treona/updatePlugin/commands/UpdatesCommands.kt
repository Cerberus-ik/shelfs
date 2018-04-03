package de.treona.updatePlugin.commands

import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.permission.Permission
import de.treona.shelfs.permission.PermissionUtil
import de.treona.shelfs.permission.StringPermission
import kotlinx.coroutines.experimental.launch
import net.dv8tion.jda.core.entities.*

class UpdatesCommands : PrivateCommand, GuildCommand {

    override fun getPermission(): Permission? {
        return null
    }

    override fun getName(): String {
        return "Updates"
    }

    override fun getDescription(): String {
        return "Notifies you whenever a plugin update is published."
    }

    override fun execute(author: User?, message: Message?, textChannel: PrivateChannel?) {
        if (author != null) {
            this.switchUpdates(author)
        }
    }

    override fun execute(author: Member?, message: Message?, textChannel: TextChannel?) {
        if (author != null) {
            this.switchUpdates(author.user)
        }
    }

    private fun switchUpdates(user: User) {
        val getsUpdates = PermissionUtil.hasPermission(user, StringPermission("update.plugins"))
        val message: String
        message = if (getsUpdates) {
            PermissionUtil.removePermission(user, StringPermission("update.plugins"))
            "You will no longer receive plugin updates."
        } else {
            PermissionUtil.addPermission(user, StringPermission("update.plugins"))
            "You will now receive plugin updates."
        }
        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        launch {
            val privateChannel = user.openPrivateChannel().complete()
            privateChannel.sendMessage(message).complete()
        }
    }
}