package de.treona.updatePlugin

import org.json.JSONObject

class PluginVersion(val name: String, val version: String) {

    companion object {
        fun fromJSON(jsonPlugin: JSONObject): PluginVersion {
            return PluginVersion(jsonPlugin.getString("name"), jsonPlugin.getString("version"))
        }
    }

    fun toJSON(): JSONObject {
        val pluginObject = JSONObject()
        pluginObject.put("name", name)
        pluginObject.put("version", version)
        return pluginObject
    }
}

data class UpdatePluginVersion(val name: String, val version: String, val newPlugin: Boolean)