package de.treona.shelfs.api.plugin;

import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.logger.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.*;

@SuppressWarnings("WeakerAccess")
public class JarFileBuilder {

    private JarOutputStream jarOutputStream;
    private List<String> directories;
    private List<String> classes;
    private HashMap<String, JSONObject> defaultConfigs;
    private HashMap<File, JSONObject> pluginDescriptions;
    private Logger logger;

    public JarFileBuilder() {
        try {
            this.logger = new Logger("JarFileBuilder");
            this.classes = new ArrayList<>();
            this.directories = new ArrayList<>();
            this.pluginDescriptions = new HashMap<>();
            this.defaultConfigs = new HashMap<>();
            this.jarOutputStream = new JarOutputStream(new FileOutputStream("bot.jar"), this.buildManifest());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addJar(File source, boolean isPlugin) {
        try (JarFile jar = new JarFile(source)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            for (Enumeration entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (entry.getName().contains("META-INF"))
                    continue;
                else if (entry.getName().equalsIgnoreCase("plugin.json")) {
                    this.pluginDescriptions.put(source, this.getJSONObjectFromJar(jar, entry));
                    continue;
                } else if (entry.getName().equalsIgnoreCase("config.json")) {
                    continue;
                } else if (entry.isDirectory() && this.directories.contains(entry.getName()))
                    continue;
                else if (entry.isDirectory())
                    this.directories.add(entry.getName());
                if (!entry.isDirectory() && this.classes.contains(entry.getName()))
                    continue;
                else if (!entry.isDirectory())
                    this.classes.add(entry.getName());
                try (InputStream entryStream = jar.getInputStream(entry)) {
                    this.jarOutputStream.putNextEntry(entry);
                    while ((bytesRead = entryStream.read(buffer)) != -1)
                        this.jarOutputStream.write(buffer, 0, bytesRead);
                }
            }
            if (isPlugin && !this.pluginDescriptions.containsKey(source))
                this.logger.logMessage(source.getName() + " does not contain a plugin.json.", LogLevel.WARNING);
            else if (isPlugin && this.pluginDescriptions.containsKey(source))
                this.addDefaultConfig(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDefaultConfig(File source) {
        try (JarFile jar = new JarFile(source)) {
            for (Enumeration entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!entry.getName().equalsIgnoreCase("config.json"))
                    continue;
                this.defaultConfigs.put(this.pluginDescriptions.get(source).getString("name"), this.getJSONObjectFromJar(jar, entry));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Manifest buildManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "de.treona.shelfs.ShelfsLoader");
        return manifest;
    }

    private JSONObject getJSONObjectFromJar(JarFile jarFile, JarEntry jarEntry) {
        try (InputStream entryStream = jarFile.getInputStream(jarEntry)) {
            return new JSONObject(IOUtils.toString(entryStream, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject("{\"error\": \"Could not read " + jarEntry.getName() + "\"}");
    }

    public void writeDefaultConfigs() throws IOException {
        JSONObject jsonObject = new JSONObject();
        this.defaultConfigs.forEach(jsonObject::put);
        this.writeJarEntry("configs.json", jsonObject.toString());
    }

    public void writePluginDescriptions() throws IOException {
        JSONArray jsonArray = new JSONArray();
        this.pluginDescriptions.forEach((key, value) -> jsonArray.put(value));
        this.writeJarEntry("plugins.json", jsonArray.toString());
    }

    private void writeJarEntry(String entryName, String content) throws IOException {
        JarEntry jarEntry = new JarEntry(entryName);
        this.jarOutputStream.putNextEntry(jarEntry);
        IOUtils.write(content.getBytes(), this.jarOutputStream);
    }

    public void close() {
        try {
            this.jarOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
