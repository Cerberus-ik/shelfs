package de.treona.leagueTools.util

import no.stelar7.api.l4j8.basic.constants.api.Platform

class RegionUtil {

    companion object {
        val regions = HashMap<Platform, MutableList<String>>()

        fun getRegionByName(name: String): Platform {
            var result = Platform.UNKNOWN
            regions.forEach { platform, names ->
                if (names.contains(name.toUpperCase())) {
                    result = platform
                    return@forEach
                }
            }
            return result
        }

        fun getPrettyRegionName(region: Platform): String {
            return when (region) {
                Platform.LA1 -> "Lan"
                Platform.LA2 -> "Las"
                else -> {
                    val regionName = Regex("[^A-Z]").replace(region.name, "")
                    regionName.substring(0, 1) + regionName.substring(1, regionName.length).toLowerCase()
                }
            }
        }
    }

    init {
        Platform.values().forEach { regions[it] = arrayListOf(it.name) }
        regions[Platform.EUW1]?.add("EUW")
        regions[Platform.EUN1]?.add("EUN")
        regions[Platform.EUN1]?.add("EUNE")
        regions[Platform.NA1]?.add("NA")
    }
}