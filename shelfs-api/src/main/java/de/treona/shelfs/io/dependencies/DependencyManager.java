package de.treona.shelfs.io.dependencies;

import de.treona.shelfs.api.Shelfs;
import de.treona.shelfs.config.Config;
import de.treona.shelfs.io.logger.LogLevel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class DependencyManager {

    private final Config config;

    public DependencyManager(Config config) {
        this.config = config;
    }

    public void loadDependencies() {
        if(this.config.libraries.size() != 0)
            this.checkDependencies();
        this.addToRuntime();
    }

    private void checkDependencies(){
        this.config.libraries.forEach(dependency ->{
            File dependencyFile = new File("libs/" + dependency.getJarName());
            if(dependencyFile.exists())
                return;
            Shelfs.getLogger().logMessage("Downloading dependency: " + dependency.getName(), LogLevel.INFO);
            try {
                FileUtils.copyURLToFile(dependency.getUrl(), dependencyFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void addToRuntime(){
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            this.config.libraries.forEach(dependency -> {
                File dependencyFile = new File("libs/" + dependency.getJarName());
                if(!dependencyFile.exists())
                    return;
                try {
                    method.invoke(classLoader, dependencyFile.toURI().toURL());
                } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
                    e.printStackTrace();
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
