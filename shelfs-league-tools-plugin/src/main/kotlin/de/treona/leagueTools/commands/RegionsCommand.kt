package de.treona.leagueTools.commands

import de.treona.leagueTools.util.RegionUtil
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.*
import java.awt.Color

class RegionsCommand : GuildCommand, PrivateCommand {
    override fun getName(): String {
        return "Regions"
    }

    override fun getDescription(): String {
        return "Shows you all of the available regions"
    }

    override fun getPermission(): Permission? {
        return null
    }

    override fun execute(author: User?, message: Message?, textChannel: PrivateChannel?) {
        message?.let { this.sendRegionsMessage(it) }
    }

    override fun execute(author: Member?, message: Message?, textChannel: TextChannel?) {
        message?.let { this.sendRegionsMessage(it) }
    }

    private fun sendRegionsMessage(message: Message) {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setTitle("Available regions: ${RegionUtil.regions.keys.size}")
        embedBuilder.setColor(Color.MAGENTA)
        RegionUtil.regions.forEach { region, names ->
            val stringBuilder = StringBuilder()
            names.forEach { stringBuilder.append(it).append(" ") }
            embedBuilder.addField(region.name, stringBuilder.toString(), true)
        }
        message.channel.sendMessage(embedBuilder.build()).queue()
    }
}