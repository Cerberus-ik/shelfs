package de.treona.shelfs.api.plugin;

import com.google.gson.Gson;
import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.exceptions.InvalidPluginDescriptionException;
import de.treona.shelfs.api.exceptions.NoPluginDescriptionException;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.logger.Logger;
import de.treona.shelfs.io.resource.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    private Logger logger;

    public PluginLoader() {
        this.logger = new Logger("PluginLoader");
    }

    //TODO rework this mess
    private ShelfsPlugin loadPluginFromJar(File file) {
        if (file == null) {
            throw new NullPointerException("Plugin file can't be null.");
        }
        URLClassLoader classLoader = null;
        try {
            final PluginDescription pluginDescription = this.loadPluginDescription(file);
            final JSONObject defaultConfig = this.loadDefaultConfig(file);
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
            this.setPluginFields(shelfsPlugin, pluginDescription, defaultConfig);
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
                assert classLoader != null;
                classLoader.close();
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private ShelfsPlugin loadPlugin(JSONObject pluginFile) {
        try {
            PluginDescription pluginDescription = new Gson().fromJson(pluginFile.toString(), PluginDescription.class);
            Class<?> jarClass = Class.forName(pluginDescription.getMain());
            Class<? extends ShelfsPlugin> pluginClass = jarClass.asSubclass(ShelfsPlugin.class);
            ShelfsPlugin shelfsPlugin = pluginClass.getDeclaredConstructor().newInstance();
            ResourceLoader resourceLoader = new ResourceLoader();
            this.setPluginFields(shelfsPlugin, pluginDescription, new JSONObject(resourceLoader.getResourceFileContent("configs.json")).getJSONObject(pluginDescription.getName()));
            return shelfsPlugin;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setPluginFields(ShelfsPlugin shelfsPlugin, PluginDescription pluginDescription, JSONObject defaultConfig) {
        try {
            shelfsPlugin.getClass().getSuperclass().getField("pluginDescription").set(shelfsPlugin, pluginDescription);
            shelfsPlugin.getClass().getSuperclass().getField("logger").set(shelfsPlugin, new Logger(shelfsPlugin));
            shelfsPlugin.getClass().getSuperclass().getField("defaultConfig").set(shelfsPlugin, defaultConfig);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    List<ShelfsPlugin> loadPlugins(JSONArray pluginFiles) {
        List<ShelfsPlugin> plugins = new ArrayList<>();
        for (int i = 0; i < pluginFiles.length(); i++) {
            ShelfsPlugin shelfsPlugin = loadPlugin(pluginFiles.getJSONObject(i));
            plugins.add(shelfsPlugin);
            if (shelfsPlugin == null)
                continue;
            Shelfs.getLogger().logMessage("Loaded: " + shelfsPlugin.getPluginDescription().getName() + " v" + shelfsPlugin.getPluginDescription().getVersion(), LogLevel.INFO);
        }
        plugins = plugins.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return plugins;
    }

    List<ShelfsPlugin> loadPlugins(File pluginDirectory) {
        final File[] files = pluginDirectory.listFiles(pathname -> pathname.getName().endsWith(".jar"));
        assert files != null;
        List<ShelfsPlugin> plugins = new ArrayList<>();
        for (File file : files) {
            ShelfsPlugin shelfsPlugin = this.loadPluginFromJar(file);
            plugins.add(shelfsPlugin);
            if (shelfsPlugin == null)
                continue;
            Shelfs.getLogger().logMessage("Loaded: " + shelfsPlugin.getPluginDescription().getName() + " v" + shelfsPlugin.getPluginDescription().getVersion(), LogLevel.INFO);
        }
        plugins = plugins.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return plugins;
    }

    private JSONObject loadDefaultConfig(File file) {
        try {
            String content = this.readFileFromJar(file, "config.json");
            if (content == null)
                return new JSONObject();
            return new JSONObject(content);
        } catch (FileNotFoundException e) {
            return new JSONObject();
        } catch (JSONException e) {
            this.logger.logMessage("The default config from " + file.getName() + " is not valid json.", LogLevel.ERROR);
        }
        return new JSONObject();
    }

    private PluginDescription loadPluginDescription(File file) throws InvalidPluginDescriptionException, NoPluginDescriptionException {
        JSONObject pluginDescriptionJsonObject;
        try {
            String content = this.readFileFromJar(file, "plugin.json");
            if (content == null)
                throw new NoPluginDescriptionException();
            pluginDescriptionJsonObject = new JSONObject(content);
        } catch (FileNotFoundException e) {
            throw new NoPluginDescriptionException();
        }
        if (isPluginDescriptionInvalid(pluginDescriptionJsonObject)) {
            throw new InvalidPluginDescriptionException();
        }
        return new Gson().fromJson(pluginDescriptionJsonObject.toString(), PluginDescription.class);
    }

    private String readFileFromJar(File file, String fileName) throws FileNotFoundException {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry jarEntry = jarFile.getJarEntry(fileName);
            if (jarEntry == null) {
                throw new FileNotFoundException("File " + fileName + " does not exist.");
            }
            StringBuilder stringBuilder = new StringBuilder();
            IOUtils.readLines(jarFile.getInputStream(jarEntry), "UTF-8").forEach(stringBuilder::append);
            return stringBuilder.toString();
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException))
                e.printStackTrace();
            else
                throw (FileNotFoundException) e;
        }
        return null;
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
