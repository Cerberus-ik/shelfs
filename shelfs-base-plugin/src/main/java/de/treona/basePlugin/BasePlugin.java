package de.treona.basePlugin;

import de.treona.basePlugin.commands.*;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.permission.GuildRolePermission;
import net.dv8tion.jda.core.entities.Role;
import org.json.JSONObject;

import java.util.HashMap;

public class BasePlugin extends ShelfsPlugin {

    @Override
    public void onEnable() {
        JSONObject config = super.getConfig();

        Shelfs.getCommandManager().registerCommand(this, "version", new VersionCommand(super.getPluginDescription().getVersion()));
        Shelfs.getCommandManager().registerCommand(this, "plugins", new PluginsCommand());
        Shelfs.getCommandManager().registerCommand(this, "load", new LoadCommand(this.getLoadRole(config)));
        Shelfs.getCommandManager().registerCommand(this, "unload", new UnloadCommand(this.getUnloadRole(config)));
        Shelfs.getCommandManager().registerCommand(this, "commands", new CommandsCommand());
    }

    private GuildRolePermission getLoadRole(JSONObject config) {
        HashMap<Long, Role> roles = new HashMap<>();
        for (int i = 0; i < config.getJSONArray("loadRoles").length(); i++) {
            JSONObject guildObject = config.getJSONArray("loadRoles").getJSONObject(i);
            Role role = Shelfs.getJda().getRoleById(guildObject.getLong("roleId"));
            if (role == null) {
                super.getLogger().logMessage("The load role for " + guildObject.getLong("guildId") + " does not exist.");
                continue;
            }
            roles.put(guildObject.getLong("guildId"), role);
        }
        return new GuildRolePermission(roles);
    }

    private GuildRolePermission getUnloadRole(JSONObject config) {
        HashMap<Long, Role> roles = new HashMap<>();
        for (int i = 0; i < config.getJSONArray("unloadRoles").length(); i++) {
            JSONObject guildObject = config.getJSONArray("unloadRoles").getJSONObject(i);
            Role role = Shelfs.getJda().getRoleById(guildObject.getLong("roleId"));
            if (role == null) {
                super.getLogger().logMessage("The unloadRoles role for " + guildObject.getLong("guildId") + " does not exist.");
                continue;
            }
            roles.put(guildObject.getLong("guildId"), role);
        }
        return new GuildRolePermission(roles);
    }
}
