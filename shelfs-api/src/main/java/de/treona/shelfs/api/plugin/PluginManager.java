package de.treona.shelfs.api.plugin;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.resource.ResourceLoader;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unused"})
public class PluginManager {

    private HashMap<ShelfsPlugin, Boolean> plugins;

    public PluginManager() {
        this.plugins = new HashMap<>();
    }

    public void loadPlugins() {
        PluginLoader pluginLoader = new PluginLoader();
        pluginLoader.loadPlugins(new JSONArray(new ResourceLoader().getResourceFileContent("plugins.json"))).forEach(this::enablePlugin);
    }

    public void enablePlugin(ShelfsPlugin shelfsPlugin) {
        if (this.plugins.containsKey(shelfsPlugin)) {
            throw new IllegalStateException("Plugin is already loaded.");
        }
        this.plugins.put(shelfsPlugin, true);
        Shelfs.getLogger().logMessage("Enabling " + shelfsPlugin.getPluginDescription().getName() + "...", LogLevel.INFO);
        shelfsPlugin.onEnable();
    }

    public void disablePlugin(ShelfsPlugin shelfsPlugin) {
        if (!this.plugins.containsKey(shelfsPlugin)) {
            throw new IllegalStateException("Plugin is not loaded.");
        }
        this.plugins.put(shelfsPlugin, false);
        Shelfs.getCommandManager().unregisterCommand(shelfsPlugin);
    }

    public List<ShelfsPlugin> getEnabledPlugins() {
        return this.plugins.keySet().stream().filter(plugin -> this.plugins.get(plugin)).collect(Collectors.toList());
    }

    public List<ShelfsPlugin> getDisabledPlugins() {
        return this.plugins.keySet().stream().filter(plugin -> !this.plugins.get(plugin)).collect(Collectors.toList());
    }

    public List<ShelfsPlugin> getPlugins() {
        return new ArrayList<>(this.plugins.keySet());
    }

    public boolean isPluginEnabled(ShelfsPlugin plugin) {
        Boolean isLoaded = this.plugins.getOrDefault(plugin, null);
        if (isLoaded == null) {
            throw new IllegalStateException("Plugin does not exist.");
        }
        return isLoaded;
    }
}
