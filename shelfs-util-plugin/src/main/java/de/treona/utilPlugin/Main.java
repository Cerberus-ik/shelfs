package de.treona.utilPlugin;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.utilPlugin.commands.ClearCommand;
import de.treona.utilPlugin.commands.JoinRoleCommand;
import de.treona.utilPlugin.commands.OwnerCommand;
import de.treona.utilPlugin.listener.JoinListener;

public class Main extends ShelfsPlugin {

    @Override
    public void onEnable() {
        Shelfs.getCommandManager().registerCommand(this, "clear", new ClearCommand());
        Shelfs.getCommandManager().registerCommand(this, "joinRole", new JoinRoleCommand(this));
        Shelfs.getCommandManager().registerCommand(this, "owner", new OwnerCommand());

        Shelfs.getJda().addEventListener(new JoinListener(this));
    }
}
