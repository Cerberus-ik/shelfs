package de.treona.musicPlugin.commands;

import de.treona.musicPlugin.audio.AudioController;
import de.treona.musicPlugin.audio.TemporaryPlayer;
import de.treona.musicPlugin.config.ConfigManager;
import de.treona.musicPlugin.config.GuildSettings;
import de.treona.musicPlugin.permission.SettingsPermission;
import de.treona.shelfs.commands.GuildCommand;
import de.treona.shelfs.permission.Permission;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.List;

public class SettingsCommand implements GuildCommand {

    private ConfigManager configManager;
    private AudioController audioController;

    public SettingsCommand(ConfigManager configManager, AudioController audioController) {
        this.configManager = configManager;
        this.audioController = audioController;
    }

    @Override
    public void execute(Member member, Message message, TextChannel textChannel) {
        String[] args = message.getContentRaw().split(" ");
        if (args.length == 1) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Available settings.");
            embedBuilder.setColor(Color.RED);
            embedBuilder.addField("Auto Playlist", "Will play when no more songs are in the playlist.", true);
            embedBuilder.addField("Max Song length", "Songs longer than this (in seconds) can only be played by the guild owner.", true);
            embedBuilder.addField("Music channel", "The bot will post updates (current songs etc) in this channel", true);
            embedBuilder.addField("Show", "Will show you all the current guild settings.", true);
            textChannel.sendMessage(embedBuilder.build()).queue();
            return;
        }
        String setting = this.getSetting(args);
        if (setting.equalsIgnoreCase("maxSongLength")) {
            try {
                int rawLength = Integer.parseInt(args[args.length - 1]);
                int songLength = Math.max(Math.min(rawLength, 36000), 0);
                GuildSettings guildSettings = this.configManager.getGuildSettings(member.getGuild());
                guildSettings.maxSongLength = songLength;
                this.configManager.saveGuildSettings(member.getGuild(), guildSettings);
                textChannel.sendMessage(songLength + "s is the new maximum song length.").queue();
            } catch (NumberFormatException e) {
                textChannel.sendMessage(args[args.length - 1] + " is not a valid song length (60-3600s).").queue();
            }
        } else if (setting.equalsIgnoreCase("autoPlayList")) {
            String url = args[args.length - 1];
            GuildSettings guildSettings = this.configManager.getGuildSettings(member.getGuild());
            if (url.equalsIgnoreCase("autoPlaylist")) {
                if (guildSettings.autoPlaylist == null || guildSettings.autoPlaylist.length() < 4) {
                    textChannel.sendMessage("You have no auto playlist set.").queue();
                    return;
                }
                if (args.length > 2 && args[2].equalsIgnoreCase("remove")) {
                    textChannel.sendMessage("Your auto playlist got removed.").queue();
                    guildSettings.autoPlaylist = null;
                    this.configManager.saveGuildSettings(member.getGuild(), guildSettings);
                    return;
                }
                textChannel.sendMessage("Your auto playlist: " + guildSettings.autoPlaylist).queue();
                return;
            }
            TemporaryPlayer temporaryPlayer = this.audioController.generateTemporaryPlayer();
            if (temporaryPlayer.isValid(url)) {
                guildSettings.autoPlaylist = url;
                this.configManager.saveGuildSettings(member.getGuild(), guildSettings);
                textChannel.sendMessage("Auto playlist got set successfully.").queue();
            } else {
                textChannel.sendMessage("I could not find a playlist under:  " + url).queue();
            }
            temporaryPlayer.destroy();
        } else if (setting.equalsIgnoreCase("musicChannel")) {
            List<TextChannel> channels = message.getMentionedChannels();
            if (channels.size() == 0) {
                textChannel.sendMessage("Please mention one channel with #channel").queue();
            } else if (channels.size() == 1) {
                GuildSettings guildSettings = this.configManager.getGuildSettings(member.getGuild());
                guildSettings.musicChannel = channels.get(0);
                this.configManager.saveGuildSettings(member.getGuild(), guildSettings);
                textChannel.sendMessage("Music channel set.").queue();
            } else {
                textChannel.sendMessage("Please mention only one channel.").queue();
            }
        } else if (setting.equalsIgnoreCase("show")) {
            GuildSettings guildSettings = this.configManager.getGuildSettings(member.getGuild());
            TextChannel targetChannel = this.requireNotNullOrElse(guildSettings.musicChannel, member.getGuild().getDefaultChannel());
            String mentionableTextChannel;
            if (targetChannel == null) {
                mentionableTextChannel = "No channels available.";
            } else {
                mentionableTextChannel = targetChannel.getAsMention();
            }
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Settings for: " + member.getGuild().getName());
            embedBuilder.setColor(Color.PINK);
            embedBuilder.addField("Music channel", mentionableTextChannel, true);
            embedBuilder.addField("Max song length", String.valueOf(guildSettings.maxSongLength), true);
            embedBuilder.addField("Auto playlist", guildSettings.autoPlaylist, true);
            embedBuilder.addField("Volume", String.valueOf(guildSettings.volume), true);

            try {
                embedBuilder.addField("DJ Role", guildSettings.djRole.getAsMention(), true);
            } catch (NullPointerException ignore) {
                embedBuilder.addField("DJ Role", "Not set", true);
            }
            try {
                embedBuilder.addField("Settings Role", guildSettings.settingsRole.getAsMention(), true);
            } catch (NullPointerException ignore) {
                embedBuilder.addField("Settings Role", "Not set", true);
            }
            textChannel.sendMessage(embedBuilder.build()).queue();
        } else {
            textChannel.sendMessage("That's not a valid setting.").queue();
        }
    }

    private String getSetting(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        int ignoredArgs = 1;
        if (args.length == 2) {
            ignoredArgs = 0;
        }
        for (int i = 1; i < args.length - ignoredArgs; i++) {
            stringBuilder.append(args[i]);
        }
        return stringBuilder.toString();
    }

    private TextChannel requireNotNullOrElse(TextChannel targetChannel, TextChannel alternative) {
        if (targetChannel == null) {
            return alternative;
        }
        return targetChannel;
    }

    @Override
    public String getName() {
        return "Settings";
    }

    @Override
    public String getDescription() {
        return "Lets you set all available music bot settings.";
    }

    @Override
    public Permission getPermission() {
        return new SettingsPermission();
    }
}
