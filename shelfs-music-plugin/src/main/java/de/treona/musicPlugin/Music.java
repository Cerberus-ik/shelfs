package de.treona.musicPlugin;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.commands.*;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.events.NowPlayingListener;
import de.treona.musicPlugin.events.PlayerLeaveListener;
import de.treona.musicPlugin.events.SearchListener;
import de.treona.musicPlugin.permission.AudioPermissionUtil;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.ShelfsPlugin;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.resource.ResourceLoader;
import net.dv8tion.jda.core.entities.Game;

public class Music extends ShelfsPlugin {

    @Override
    public void onEnable() {
        if (!this.checkForNativeLibraries()) {
            Shelfs.getPluginManager().disablePlugin(this);
            return;
        }

        Shelfs.getJda().getPresence().setGame(Game.of(Game.GameType.LISTENING, Shelfs.getCommandManager().getCommandPrefix() + "play song"));
        ConfigManager configManager = new ConfigManager(this);
        configManager.loadConfig();
        new AudioPermissionUtil(configManager);
        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioController audioController = new AudioController(configManager);

        Shelfs.getJda().addEventListener(new PlayerLeaveListener(audioController));
        Shelfs.getJda().addEventListener(new NowPlayingListener(audioController));
        Shelfs.getJda().addEventListener(new SearchListener(audioController, configManager));

        Shelfs.getCommandManager().registerCommand(this, "djRole", new DJRoleCommand(configManager));
        Shelfs.getCommandManager().registerCommand(this, "settingsRole", new SettingsRoleCommand(configManager));
        Shelfs.getCommandManager().registerCommand(this, "play", new PlayCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "join", new JoinCommand());
        Shelfs.getCommandManager().registerCommand(this, "leave", new LeaveCommand());
        Shelfs.getCommandManager().registerCommand(this, "nowPlaying", new NowPlayingCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "volume", new VolumeCommand(audioController, configManager));
        Shelfs.getCommandManager().registerCommand(this, "skip", new SkipCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "pause", new PauseCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "stop", new StopCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "queue", new QueueCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "repeat", new RepeatCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "replay", new ReplayCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "shuffle", new ShuffleCommand(audioController));
        Shelfs.getCommandManager().registerCommand(this, "settings", new SettingsCommand(configManager, audioController));
    }

    private boolean checkForNativeLibraries() {
        ResourceLoader resourceLoader = new ResourceLoader();
        if (!resourceLoader.doesResourceExist("natives/win-x86/connector.dll")) {
            super.getLogger().logMessage("Native libraries are missing!", LogLevel.ERROR);
            super.getLogger().logMessage("Dynamic load can cause issues with native libraries.", LogLevel.ERROR);
            return false;
        }
        return true;
    }
}
