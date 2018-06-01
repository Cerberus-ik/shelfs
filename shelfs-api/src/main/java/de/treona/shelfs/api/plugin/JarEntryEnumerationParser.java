package de.treona.shelfs.api.plugin;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

class JarEntryEnumerationParser {

    static List<JarEntry> getValidEntries(Enumeration<JarEntry> entries) {
        List<JarEntry> validEntries = new ArrayList<>();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().contains(".")) {
                continue;
            }
            validEntries.add(entry);
        }
        return validEntries;
    }
}
