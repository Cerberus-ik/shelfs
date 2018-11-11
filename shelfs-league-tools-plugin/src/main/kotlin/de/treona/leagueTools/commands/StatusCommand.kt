package de.treona.leagueTools.commands

import de.treona.leagueTools.util.RegionUtil
import de.treona.shelfs.commands.GuildCommand
import de.treona.shelfs.commands.PrivateCommand
import de.treona.shelfs.permission.Permission
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.*
import no.stelar7.api.l4j8.basic.constants.api.Platform
import no.stelar7.api.l4j8.impl.raw.StatusAPI
import no.stelar7.api.l4j8.pojo.status.Service
import java.awt.Color

class StatusCommand : PrivateCommand, GuildCommand {

    override fun getName(): String {
        return "Status"
    }

    override fun getDescription(): String {
        return "Shows the server status messages"
    }

    override fun getPermission(): Permission? {
        return null
    }

    override fun execute(author: User?, message: Message?, textChannel: PrivateChannel?) {
        message?.let { author?.let { _ -> this.sendServerStatusMessage(it) } }
    }

    override fun execute(author: Member?, message: Message?, textChannel: TextChannel?) {
        message?.let { author?.let { _ -> this.sendServerStatusMessage(it) } }
    }

    private fun getMessageColor(services: MutableList<Service>): Color{
        var info = false
        var warning = false
        services.forEach { service -> service.incidents.map { it.updates.first().severity }.forEach {
            when(it){
                "info" -> info = true
                "warning" -> warning = true
                "error" -> return Color.RED
                else -> return Color.RED
            }
        } }
        if(warning)
            return Color.YELLOW
        else if(info)
            return Color.BLUE
        return Color.GREEN
    }

    private fun getReplacementString(inputString: String) : String {
        return when(inputString){
            "info" -> "❕"
            "warning" -> "⚠"
            "error" -> "❗"
            else -> inputString
        }
    }

    private fun sendServerStatusMessage(message: Message) {
        val region = RegionUtil.getRegionByCommand(message)
        if (region == Platform.UNKNOWN) {
            message.channel.sendMessage("Could not identify the specified region.").queue()
            return
        }
        val messageEmbedBuilder = EmbedBuilder()
        val services = StatusAPI.getInstance().getShardStatus(region).services
        messageEmbedBuilder.setColor(this.getMessageColor(services))
        if (services.map { it.incidents }.count() - 4 > 0) {
            messageEmbedBuilder.setTitle("There are ${services.map { it.incidents }.count() - 4} incidents right now")
            services.forEach { service ->
                service.incidents.forEach { incident ->
                    val stringBuilder = StringBuilder()
                    for(update in incident.updates){
                           stringBuilder.append(this.getReplacementString(update.severity)).append(" ").append(update.content)
                    }
                    messageEmbedBuilder.addField(service.name, stringBuilder.toString(), false)
                }
            }
        } else {
            messageEmbedBuilder.setTitle("No incidents right now, everything seems to work fine")
        }
        message.channel.sendMessage(messageEmbedBuilder.build()).queue()
    }
}