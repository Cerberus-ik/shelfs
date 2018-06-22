package de.treona.leagueTools.account

import de.treona.leagueTools.LeagueTools
import de.treona.leagueTools.util.RandomCodeGenerator
import net.dv8tion.jda.core.entities.User
import no.stelar7.api.l4j8.impl.raw.ThirdPartyAPI

class RegistrationManager {

    private val registrations = HashMap<DiscordSummoner, String>()
    private val randomCodeGenerator = RandomCodeGenerator()

    fun isInRegistration(user: User): Boolean {
        return this.registrations.keys.stream().anyMatch { it.user == user }
    }

    fun startRegistering(discordSummoner: DiscordSummoner): String {
        val code = this.randomCodeGenerator.generateCode(12)
        this.registrations[discordSummoner] = code
        return code
    }

    fun valid(user: User): Boolean {
        val discordSummoner = this.registrations.keys.stream().filter { it.user == user }.findAny().get()
        val code = ThirdPartyAPI.getInstance().getCode(discordSummoner.region, discordSummoner.summonerId)
        return this.registrations[discordSummoner] == code
    }

    fun register(user: User) {
        val discordSummoner = this.registrations.keys.stream().filter { it.user == user }.findAny().get()
        LeagueTools.databaseManager.registerSummoner(discordSummoner)
        this.registrations.remove(discordSummoner)
    }
}