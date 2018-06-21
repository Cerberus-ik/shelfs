package de.treona.leagueTools.io

import com.google.gson.Gson

class ConfigReader {

    companion object {
        fun getDatabaseCredentials(configContent: String): DatabaseCredentialsImplementation {
            return Gson().fromJson(configContent, DatabaseCredentialsImplementation::class.java)
        }
    }
}