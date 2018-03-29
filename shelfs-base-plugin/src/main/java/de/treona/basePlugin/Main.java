package de.treona.basePlugin;

import de.treona.basePlugin.commands.*;
import de.treona.basePlugin.config.ConfigManager;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.permission.RolePermission;
import net.dv8tion.jda.core.entities.Role;
import org.json.JSONObject;

public class Main extends ShelfsPlugin {

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        JSONObject config = configManager.loadConfig();
        Role loadRole = Shelfs.getJda().getRoleById(config.getString("pluginLoadRequiredRole"));
        Role unloadRole = Shelfs.getJda().getRoleById(config.getString("pluginUnloadRequiredRole"));

        Shelfs.getCommandManager().registerCommand(this, "version", new VersionCommand(super.getPluginDescription().getVersion()));
        Shelfs.getCommandManager().registerCommand(this, "plugins", new PluginsCommand());
        Shelfs.getCommandManager().registerCommand(this, "load", new LoadCommand(new RolePermission(loadRole)));
        Shelfs.getCommandManager().registerCommand(this, "unload", new UnloadCommand(new RolePermission(unloadRole)));
        Shelfs.getCommandManager().registerCommand(this, "commands", new CommandsCommand());
    }
}
