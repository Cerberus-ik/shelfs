package de.treona.utilPlugin;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.utilPlugin.commands.ClearCommand;

public class Main extends ShelfsPlugin {

    @Override
    public void onEnable() {
        Shelfs.getCommandManager().registerCommand(this, "clear", new ClearCommand());
    }
}
