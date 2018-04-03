package de.treona.updatePlugin

import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.api.plugin.PluginDescription
import org.json.JSONArray

class VersionManager {
    companion object {
        fun getChanges(savedPlugins: List<PluginVersion>, currentPlugins: List<PluginVersion>): List<UpdatePluginVersion> {
            val changes: MutableList<UpdatePluginVersion> = mutableListOf()
            currentPlugins.forEach {
                if (isPluginUpdated(it, savedPlugins) && isPluginNew(it, savedPlugins))
                    changes.add(UpdatePluginVersion(it.name, it.version, true))
                else if (isPluginUpdated(it, savedPlugins))
                    changes.add(UpdatePluginVersion(it.name, it.version, false))
            }
            return changes
        }

        fun loadSavedVersions(rawData: JSONArray): List<PluginVersion> {
            val plugins: MutableList<PluginVersion> = mutableListOf()
            if (rawData.length() == 0) {
                return plugins
            }
            for (i in 0 until rawData.length())
                plugins.add(PluginVersion.fromJSON(rawData.getJSONObject(i)))
            return plugins
        }

        fun getLoadedPlugins(): List<PluginVersion> {
            val plugins: MutableList<PluginVersion> = mutableListOf()
            Shelfs.getPluginManager().plugins.forEach {
                val pluginDescription: PluginDescription = it.pluginDescription
                plugins.add(PluginVersion(pluginDescription.name, pluginDescription.version))
            }
            return plugins
        }

        private fun isPluginUpdated(plugin: PluginVersion, savedPlugins: List<PluginVersion>): Boolean {
            return savedPlugins.none { plugin.name.contentEquals(it.name) && plugin.version.contentEquals(it.version) }
        }

        private fun isPluginNew(plugin: PluginVersion, savedPlugins: List<PluginVersion>): Boolean {
            return savedPlugins.none { it.name == plugin.name }
        }
    }
}