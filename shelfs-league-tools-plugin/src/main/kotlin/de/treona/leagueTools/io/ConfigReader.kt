package de.treona.leagueTools.io

import de.treona.shelfs.config.ConfigManager
import de.treona.shelfs.io.database.DatabaseCredentials
import org.json.JSONObject

class ConfigReader {

    companion object {
        fun getDatabaseCredentials(configContent: String): DatabaseCredentials {
            val method = ConfigManager::class.java.getDeclaredMethod("loadDatabaseCredentials", JSONObject::class.java)
            method.isAccessible = true
            return method.invoke(ConfigManager(), JSONObject(configContent).getJSONObject("database")) as DatabaseCredentials
        }
    }
}