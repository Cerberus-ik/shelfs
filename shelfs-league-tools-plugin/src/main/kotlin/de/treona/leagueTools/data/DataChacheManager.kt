package de.treona.leagueTools.data

import de.treona.leagueTools.LeagueTools
import de.treona.shelfs.io.logger.LogLevel
import de.treona.shelfs.io.logger.Logger
import no.stelar7.api.l4j8.basic.constants.api.Platform
import no.stelar7.api.l4j8.basic.constants.flags.ChampDataFlags
import no.stelar7.api.l4j8.impl.raw.StaticAPI
import no.stelar7.api.l4j8.pojo.staticdata.champion.StaticChampion
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class DataCacheManager {

    private val logger = Logger("Cache")
    val champions = HashMap<Int, StaticChampion>()

    private fun getLatestVersion(): String? {
        val url = URL("https://ddragon.leagueoflegends.com/api/versions.json")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        return if (connection.responseCode != 200) {
            this.logger.logMessage("Error while requesting the latest version: ${connection.responseCode}", LogLevel.ERROR)
            connection.disconnect()
            null
        } else {
            val result = IOUtils.toString(connection.inputStream, "UTF-8")
            connection.disconnect()
            val jsonArray = JSONArray(result)
            jsonArray.getString(0)
        }
    }

    fun start() {
        this.logger.logMessage("Starting the cache manager.", LogLevel.INFO)
        val scheduledExecutorService = ScheduledThreadPoolExecutor(1)
        scheduledExecutorService.scheduleAtFixedRate({
            this.logger.logMessage("Checking for updates...", LogLevel.INFO)
            val latestVersion = this.getLatestVersion()
            val latestBotVersion = LeagueTools.databaseManager.getLatestCachedVersion()
            if (latestBotVersion != null && latestBotVersion == latestVersion) {
                this.logger.logMessage("Bot has the newest version cached.", LogLevel.INFO)
                if (champions.size == 0) {
                    this.logger.logMessage("No static champions are cached right now", LogLevel.INFO)
                    this.deserializeAndStoreAllChampions(LeagueTools.databaseManager.getCachedChampions(LeagueTools.databaseManager.getLatestVersionId()))
                    this.logger.logMessage("Loaded static champions from cache.", LogLevel.INFO)
                }
            } else if (latestVersion != null) {
                latestVersion.let { LeagueTools.databaseManager.updateLatestVersion(it) }
                this.logger.logMessage("Updated the current game version from: $latestBotVersion to: $latestVersion", LogLevel.INFO)
                val versionId = LeagueTools.databaseManager.getLatestVersionId()
                val champions = this.getChampions(latestVersion)
                LeagueTools.databaseManager.updateChampions(champions, versionId)
                this.deserializeAndStoreAllChampions(champions)
                this.logger.logMessage("Cached all static champions.", LogLevel.INFO)
            }
        }, 0, 24, TimeUnit.HOURS)
    }

    private fun deserializeAndStoreAllChampions(champions: HashMap<Int, String>) {
        champions.forEach { championId, base64Data ->
            this.champions[championId] = this.deserializeObject(base64Data) as StaticChampion
        }
    }

    private fun getChampions(version: String): HashMap<Int, String> {
        val champions = StaticAPI.getInstance().getChampions(Platform.EUW1, mutableSetOf(ChampDataFlags.ALL), version, "en_US")
        val base64Data = HashMap<Int, String>()
        champions.forEach { championId, staticChampion ->
            base64Data[championId] = this.serializeObject(staticChampion)
        }
        return base64Data
    }

    private fun deserializeObject(base64: String): Serializable {
        val bytes = Base64.getDecoder().decode(base64)
        val objectInputStream = ObjectInputStream(ByteArrayInputStream(bytes))
        val serializable = objectInputStream.readObject() as Serializable
        objectInputStream.close()
        return serializable
    }

    private fun serializeObject(serializable: Serializable): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(serializable)
        objectOutputStream.close()
        val result = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
        byteArrayOutputStream.close()
        return result
    }
}