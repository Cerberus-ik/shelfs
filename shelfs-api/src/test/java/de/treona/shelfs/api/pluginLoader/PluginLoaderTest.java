package de.treona.shelfs.api.pluginLoader;

import de.treona.shelfs.api.plugin.PluginLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class PluginLoaderTest {

    PluginLoader pluginLoader;

    PluginLoaderTest() {
        this.pluginLoader = new PluginLoader();
    }

    @Test
    void loadPlugin() {
    }

    @Test
    void pluginDescriptionValidator() {
        //3,5,6 should be valid
        Path path = Paths.get("src", "test", "resources", "pluginDescription");
        assertNotNull(path);
        assertNotNull(path.toFile().listFiles());
        for (File file : path.toFile().listFiles()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuilder = new StringBuilder();
                bufferedReader.lines().forEach(stringBuilder::append);
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                if (this.pluginLoader.isPluginDescriptionInvalid(jsonObject)) {
                    if (file.getName().contains("valid")) {
                        fail("Should be valid: " + file.getName());
                    } else {
                        System.out.println(file.getName() + " is not valid, correct.");
                        continue;
                    }
                }
                if (!file.getName().contains("valid")) {
                    fail("Should not be valid: " + file.getName());
                    continue;
                }
                System.out.println(file.getName() + " is valid, correct.");
            } catch (FileNotFoundException e) {
                fail("File not found");
            } catch (JSONException ignore) {
                if (file.getName().contains("valid")) {
                    fail("Should be valid: " + file.getName());
                } else {
                    System.out.println(file.getName() + " is not valid, correct.");
                }
            }
        }
    }
}