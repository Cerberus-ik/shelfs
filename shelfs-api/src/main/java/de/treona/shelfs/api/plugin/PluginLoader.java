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
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;

import java.io.*;
import java.net.MalformedURLException;
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
        try {
            final PluginDescription pluginDescription = this.loadPluginDescription(file);
            JarClassLoader jarClassLoader = new JarClassLoader();
            jarClassLoader.add(file.toURI().toURL());
            JclObjectFactory factory = JclObjectFactory.getInstance();
            ShelfsPlugin shelfsPlugin = (ShelfsPlugin) factory.create(jarClassLoader, pluginDescription.getMain());
            shelfsPlugin.getClass().getSuperclass().getField("pluginDescription").set(shelfsPlugin, pluginDescription);
            shelfsPlugin.getClass().getSuperclass().getField("logger").set(shelfsPlugin, new Logger(shelfsPlugin));
            Shelfs.getLogger().logMessage("Loaded: " + shelfsPlugin.getPluginDescription().getName() + " v" + shelfsPlugin.getPluginDescription().getVersion(), LogLevel.INFO);
            return shelfsPlugin;
        } catch (InvalidPluginDescriptionException | IllegalAccessException | NoSuchFieldException | MalformedURLException e) {
            e.printStackTrace();
        } catch (NoPluginDescriptionException e) {
            Shelfs.getLogger().logMessage("Plugin: " + file.getName() + " does not contain a valid plugin.json", LogLevel.ERROR);
        } catch (ClassCastException e) {
            Shelfs.getLogger().logMessage("Plugin: " + file.getName() + " has no valid main class.", LogLevel.ERROR);
        }
        return null;
    }

    List<ShelfsPlugin> loadPlugins(File pluginDirectory) {
        final File[] files = pluginDirectory.listFiles(pathname -> pathname.getName().endsWith(".jar"));
        assert files != null;
        List<ShelfsPlugin> plugins = new ArrayList<>();
        for (File file : files) {
            plugins.add(this.loadPlugin(file));
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
            if (this.isPluginDescriptionInvalid(pluginDescriptionJsonObject)) {
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
