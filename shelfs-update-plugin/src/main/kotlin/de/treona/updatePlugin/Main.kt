package de.treona.updatePlugin

import de.treona.shelfs.api.Shelfs
import de.treona.shelfs.api.plugin.ShelfsPlugin
import de.treona.shelfs.permission.PermissionUtil
import de.treona.shelfs.permission.StringPermission
import de.treona.updatePlugin.commands.UpdatesCommands
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import net.dv8tion.jda.core.EmbedBuilder
import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
class Main : ShelfsPlugin() {
    override fun onEnable() {
        Shelfs.getCommandManager().registerCommand(this, "updates", UpdatesCommands())
        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        launch {
            delay(5000)
            checkForUpdates()
            saveConfig()
        }
    }

    private fun saveConfig() {
        val config = JSONObject()
        val plugins = JSONArray()
        VersionManager.getLoadedPlugins().forEach {
            plugins.put(it.toJSON())
        }
        config.put("plugins", plugins)
        super.saveConfig(config)
    }

    private fun checkForUpdates() {
        var config = super.getConfig()
        if (config == null || !config.has("plugins")) {
            super.writeDefaultConfig()
            config = super.getConfig()
        }
        val oldPlugins: List<PluginVersion> = VersionManager.loadSavedVersions(config.getJSONArray("plugins"))
        val loadedPlugins: List<PluginVersion> = VersionManager.getLoadedPlugins()
        val changes: List<UpdatePluginVersion> = VersionManager.getChanges(oldPlugins, loadedPlugins)
        if (changes.isEmpty()) {
            return
        }
        this.buildMessage(changes)
    }

    private fun buildMessage(changes: List<UpdatePluginVersion>) {
        val messageBuilder = EmbedBuilder()
        messageBuilder.setTitle("${changes.size} change(s) detected!")
        changes.forEach {
            if (it.newPlugin)
                messageBuilder.addField("New plugin: ${it.name}", "Version: ${it.version}", false)
            else
                messageBuilder.addField("Updated plugin: ${it.name}", "Version: ${it.version}", false)
        }
        val message = messageBuilder.build()
        PermissionUtil.getUsersWithPermission(StringPermission("update.plugins")).forEach {
            @Suppress("EXPERIMENTAL_FEATURE_WARNING")
            launch {
                val privateChannel = it.openPrivateChannel().complete()
                privateChannel.sendMessage(message).queue()
            }
        }
    }
}