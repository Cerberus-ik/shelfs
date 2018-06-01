package de.treona.shelfs.api.plugin;

import com.google.gson.Gson;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.exceptions.InvalidPluginDescriptionException;
import de.treona.shelfs.api.exceptions.NoPluginDescriptionException;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.logger.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader {

    private ShelfsPlugin loadPlugin(File file) {
        if (file == null) {
            throw new NullPointerException("Plugin file can't be null.");
        }
        URLClassLoader classLoader = null;
        try {
            final PluginDescription pluginDescription = this.loadPluginDescription(file);
            JarFile jarFile = new JarFile(file);
            URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
            classLoader = new URLClassLoader(urls);
            List<JarEntry> entries = JarEntryEnumerationParser.getValidEntries(jarFile.entries());
            if (entries.size() == 0)
                throw new IOException();
            ShelfsPlugin shelfsPlugin = null;
            for (JarEntry entry : entries) {
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .substring(0, entry.getName().length() - 6)
                            .replaceAll("/", ".");
                    if (pluginDescription.getMain().equals(className)) {
                        Class<?> jarClass = classLoader.loadClass(className);
                        Class<? extends ShelfsPlugin> pluginClass = jarClass.asSubclass(ShelfsPlugin.class);
                        shelfsPlugin = pluginClass.getDeclaredConstructor().newInstance();
                    } else
                        classLoader.loadClass(className);
                }
            }
            if (shelfsPlugin == null)
                throw new ClassNotFoundException();
            this.setPluginFields(shelfsPlugin, pluginDescription);
            return shelfsPlugin;
        } catch (InvalidPluginDescriptionException | IllegalAccessException | IOException | InstantiationException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoPluginDescriptionException e) {
            Shelfs.getLogger().logMessage("Plugin: " + file.getName() + " does not contain a valid plugin.json", LogLevel.ERROR);
        } catch (ClassCastException e) {
            e.printStackTrace();
            Shelfs.getLogger().logMessage("Plugin: " + file.getName() + " has no valid main class.", LogLevel.ERROR);
        } finally {
            try {
                classLoader.close();
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private void setPluginFields(ShelfsPlugin shelfsPlugin, PluginDescription pluginDescription) {
        try {
            shelfsPlugin.getClass().getSuperclass().getField("pluginDescription").set(shelfsPlugin, pluginDescription);
            shelfsPlugin.getClass().getSuperclass().getField("logger").set(shelfsPlugin, new Logger(shelfsPlugin));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    List<ShelfsPlugin> loadPlugins(File pluginDirectory) {
        final File[] files = pluginDirectory.listFiles(pathname -> pathname.getName().endsWith(".jar"));
        assert files != null;
        List<ShelfsPlugin> plugins = new ArrayList<>();
        for (File file : files) {
            ShelfsPlugin shelfsPlugin = this.loadPlugin(file);
            plugins.add(shelfsPlugin);
            Shelfs.getLogger().logMessage("Loaded: " + shelfsPlugin.getPluginDescription().getName() + " v" + shelfsPlugin.getPluginDescription().getVersion(), LogLevel.INFO);
        }
        plugins = plugins.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return plugins;
    }

    private PluginDescription loadPluginDescription(File file) throws InvalidPluginDescriptionException, NoPluginDescriptionException {
        JarFile jarFile = null;
        InputStream inputStream = null;
        PluginDescription pluginDescription;
        try {
            jarFile = new JarFile(file);
            JarEntry jarEntry = jarFile.getJarEntry("plugin.json");
            if (jarEntry == null) {
                throw new InvalidPluginDescriptionException();
            }
            inputStream = jarFile.getInputStream(jarEntry);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader.lines().forEach(stringBuilder::append);
            JSONObject pluginDescriptionJsonObject = new JSONObject(stringBuilder.toString());
            if (isPluginDescriptionInvalid(pluginDescriptionJsonObject)) {
                throw new InvalidPluginDescriptionException();
            }
            pluginDescription = new Gson().fromJson(pluginDescriptionJsonObject.toString(), PluginDescription.class);
        } catch (IOException e) {
            throw new NoPluginDescriptionException();
        } catch (JSONException e) {
            throw new InvalidPluginDescriptionException();
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pluginDescription;
    }

    public boolean isPluginDescriptionInvalid(JSONObject jsonObject) {
        try (InputStream inputStream = getClass().getResourceAsStream("/schemas/pluginDescriptionSchema-1.0.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            return true;
        }
        return false;
    }
}
