package de.treona.shelfs.api.plugin;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.*;

@SuppressWarnings("WeakerAccess")
public class JarFileBuilder {

    private JarOutputStream jarOutputStream;
    private List<String> directories;
    private List<String> classes;
    private List<JSONObject> pluginDescriptions;

    public JarFileBuilder() {
        try {
            this.classes = new ArrayList<>();
            this.directories = new ArrayList<>();
            this.pluginDescriptions = new ArrayList<>();
            this.jarOutputStream = new JarOutputStream(new FileOutputStream("bot.jar"), this.buildManifest());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addJar(File source) throws IOException {
        JarFile jar = new JarFile(source);
        byte[] buffer = new byte[1024];
        int bytesRead;
        for (Enumeration entries = jar.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = (JarEntry) entries.nextElement();
            if (entry.getName().contains("META-INF"))
                continue;
            else if (entry.getName().equalsIgnoreCase("plugin.json")) {
                this.pluginDescriptions.add(this.getPluginDescriptionsFromEntry(jar, entry));
                continue;
            } else if (entry.isDirectory() && this.directories.contains(entry.getName()))
                continue;
            else if (entry.isDirectory())
                this.directories.add(entry.getName());
            if (!entry.isDirectory() && this.classes.contains(entry.getName()))
                continue;
            else if (!entry.isDirectory())
                this.classes.add(entry.getName());
            InputStream entryStream = jar.getInputStream(entry);
            this.jarOutputStream.putNextEntry(entry);
            while ((bytesRead = entryStream.read(buffer)) != -1) {
                this.jarOutputStream.write(buffer, 0, bytesRead);
            }
            entryStream.close();
        }
        jar.close();
    }

    private Manifest buildManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "de.treona.shelfs.ShelfsLoader");
        return manifest;
    }

    private JSONObject getPluginDescriptionsFromEntry(JarFile jarFile, JarEntry jarEntry) {
        try (InputStream entryStream = jarFile.getInputStream(jarEntry)) {
            return new JSONObject(IOUtils.toString(entryStream, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject("{\"error\": \"Could not read the plugin description\"}");
    }

    public void writePluginDescriptions() throws IOException {
        JarEntry jarEntry = new JarEntry("plugins.json");
        this.jarOutputStream.putNextEntry(jarEntry);
        byte[] buffer = new byte[1024];
        int bytesRead;
        JSONArray jsonArray = new JSONArray();
        this.pluginDescriptions.forEach(jsonArray::put);
        InputStream entryStream = new ByteArrayInputStream(jsonArray.toString().getBytes());
        while ((bytesRead = entryStream.read(buffer)) != -1) {
            this.jarOutputStream.write(buffer, 0, bytesRead);
        }
    }

    public void close() {
        try {
            this.jarOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
