package de.treona.shelfs.config;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.exceptions.InvalidConfigException;
import de.treona.shelfs.io.IOType;
import de.treona.shelfs.io.database.DatabaseCredentials;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.resource.JSONBeautifier;
import de.treona.shelfs.io.resource.ResourceLoader;
import net.dv8tion.jda.core.OnlineStatus;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class ConfigManager {

    private Config config;
    private File configFile;
    private File directory;

    public ConfigManager() {
        this.configFile = new File("config/config.json");
        this.directory = new File("config/");
    }

    public void load() {
        JSONObject jsonObject;
        try {
            jsonObject = this.loadConfigFile();
        } catch (InvalidConfigException e) {
            Shelfs.getLogger().logMessage("The config is not valid.", LogLevel.ERROR);
            return;
        }
        this.config = new Config();
        try {
            this.config.getClass().getDeclaredField("databaseCredentials").set(config, this.loadDatabaseCredentials(jsonObject.getJSONObject("database")));
            this.config.getClass().getDeclaredField("dynamicLoad").set(config, jsonObject.getBoolean("dynamicLoad"));
            this.config.getClass().getDeclaredField("pluginDirectory").set(config, jsonObject.getString("pluginDirectory"));
            this.config.getClass().getDeclaredField("token").set(config, jsonObject.getString("token"));
            this.config.getClass().getDeclaredField("commandPrefix").set(config, jsonObject.getString("commandPrefix"));
            this.config.getClass().getDeclaredField("currentGame").set(config, jsonObject.getString("currentGame"));
            this.config.getClass().getDeclaredField("onlineStatus").set(config, OnlineStatus.fromKey(jsonObject.getString("onlineStatus")));
            this.config.getClass().getDeclaredField("ioType").set(config, IOType.getTypeByName(jsonObject.getString("io")));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            Shelfs.getLogger().logMessage("Unknown field in " + config.getClass(), LogLevel.ERROR);
            e.printStackTrace();
        }
    }

    private DatabaseCredentials loadDatabaseCredentials(JSONObject config) {
        return new DatabaseCredentials() {
            @Override
            public int getPort() {
                return config.getInt("ioPort");
            }

            @Override
            public String getHost() {
                return config.getString("ioHost");
            }

            @Override
            public String getDatabase() {
                return config.getString("ioDb");
            }

            @Override
            public String getUser() {
                return config.getString("ioUser");
            }

            @Override
            public String getPassword() {
                return config.getString("ioPassword");
            }
        };
    }

    public Config getConfig() {
        return this.config;
    }

    private JSONObject loadConfigFile() throws InvalidConfigException {
        JSONObject config = null;
        if (!this.directory.exists()) {
            if (this.directory.mkdirs()) {
                Shelfs.getLogger().logMessage("Created the config directory.", LogLevel.INFO);
            }
        }
        if (!this.configFile.exists()) {
            try {
                if (this.configFile.createNewFile()) {
                    Shelfs.getLogger().logMessage("Created the default config file.", LogLevel.INFO);
                    this.writeDefaultConfig();
                }
            } catch (IOException e) {
                Shelfs.getLogger().logMessage("Could not create the default config file.", LogLevel.ERROR);
                e.printStackTrace();
            }

        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.configFile))) {
            bufferedReader.lines().forEach(stringBuilder::append);
            config = new JSONObject(stringBuilder.toString());
            if (!this.isConfigValid(config)) {
                throw new InvalidConfigException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new InvalidConfigException();
        }
        return config;
    }

    private void writeDefaultConfig() {
        ResourceLoader resourceLoader = new ResourceLoader();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.configFile))) {
            bufferedWriter.write(JSONBeautifier.beautifyJSONObject(new JSONObject(resourceLoader.getResourceFileContent("config/defaultConfig.json"))));
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Shelfs.getLogger().logMessage("Wrote the default config.", LogLevel.INFO);
    }

    private boolean isConfigValid(JSONObject jsonObject) {
        try (InputStream inputStream = getClass().getResourceAsStream("/schemas/configSchema-1.1.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }
}
