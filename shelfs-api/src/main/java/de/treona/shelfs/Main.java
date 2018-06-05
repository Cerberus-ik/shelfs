package de.treona.shelfs;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.api.plugin.JarFileBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {
        File pluginsDirectory = new File("plugins");
        JarFileBuilder jarFileBuilder = new JarFileBuilder();
        if (pluginsDirectory.exists()) {
            Arrays.stream(Objects.requireNonNull(pluginsDirectory.listFiles())).filter(file -> file.getName().endsWith(".jar")).forEach(file -> {
                try {
                    jarFileBuilder.addJar(file);
                    System.out.println("Packaging " + file.getName() + "...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println("Writing plugin information...");
        jarFileBuilder.writePluginDescriptions();
        System.out.println("Packaging Shelfs v" + Shelfs.getVersion() + "...");
        jarFileBuilder.addJar(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
        jarFileBuilder.close();
        System.out.println("Launching Shelfs...");
        Process process = Runtime.getRuntime().exec("java -jar bot.jar -builder");
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
