package de.treona.shelfs;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.JarFileBuilder;
import de.treona.shelfs.config.Config;
import de.treona.shelfs.config.ConfigManager;
import de.treona.shelfs.io.logger.LogLevel;
import de.treona.shelfs.io.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    private static Config config;
    private static Logger logger;

    public static void main(String[] args) throws IOException {
        logger = new Logger();
        ConfigManager configManager = new ConfigManager();
        configManager.load();
        config = configManager.getConfig();
        if (config.dynamicLoad) {
            //TODO add native libraries
            logger.logMessage("Dynamic load isn't fully supported yet.", LogLevel.WARNING);
            dynamicStart();
        } else
            staticStart();
    }

    private static void dynamicStart() {
        ShelfsLoader.start();
    }

    private static void staticStart() throws IOException {
        JarFileBuilder jarFileBuilder = new JarFileBuilder();
        packagePlugins(new File(config.pluginDirectory), jarFileBuilder);
        logger.logMessage("Packaging Shelfs v" + Shelfs.getVersion() + "...", LogLevel.INFO);
        jarFileBuilder.addJar(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()), false);
        jarFileBuilder.close();

        logger.logMessage("Launching Shelfs...", LogLevel.INFO);
        new ProcessBuilder().command("java", "-jar", "bot.jar", "-builder").inheritIO().start();
        //Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
        /*BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null)
            System.out.println(line);*/
    }

    private static void packagePlugins(File pluginDirectory, JarFileBuilder jarFileBuilder) throws IOException {
        if (pluginDirectory.exists()) {
            Arrays.stream(Objects.requireNonNull(pluginDirectory.listFiles())).filter(file -> file.getName().endsWith(".jar")).forEach(file -> {
                jarFileBuilder.addJar(file, true);
                logger.logMessage("Packaging " + file.getName() + "...", LogLevel.INFO);
            });
        }
        logger.logMessage("Writing plugin information...", LogLevel.INFO);
        jarFileBuilder.writePluginDescriptions();
        logger.logMessage("Writing default configs...", LogLevel.INFO);
        jarFileBuilder.writeDefaultConfigs();
    }
}
