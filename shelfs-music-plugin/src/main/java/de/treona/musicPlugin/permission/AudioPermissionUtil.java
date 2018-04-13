package de.treona.musicPlugin.permission;

import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.config.GuildSettings;
import net.dv8tion.jda.core.entities.Member;

public class AudioPermissionUtil {

    private static ConfigManager configManager;

    public AudioPermissionUtil(ConfigManager configManager) {
        AudioPermissionUtil.configManager = configManager;
    }

    public static boolean hasAudioPermission(Member member) {
        if (member.isOwner()) {
            return true;
        }
        GuildSettings guildSettings = configManager.getGuildSettings(member.getGuild());
        return member.getRoles().contains(guildSettings.djRole);
    }

    public static boolean hasSettingsPermission(Member member) {
        if (member.isOwner())
            return true;
        GuildSettings guildSettings = configManager.getGuildSettings(member.getGuild());
        return member.getRoles().contains(guildSettings.settingsRole);
    }
}
