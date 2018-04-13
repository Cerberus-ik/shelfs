package de.treona.musicPlugin.util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchManager {

    private HashMap<Guild, List<AudioTrack>> searches;

    public SearchManager() {
        this.searches = new HashMap<>();
    }

    public void addSearch(Guild guild, List<AudioTrack> tracks) {
        this.searches.put(guild, tracks);
    }

    public List<AudioTrack> getSearch(Guild guild) {
        return this.searches.getOrDefault(guild, new ArrayList<>());
    }
}
