package de.treona.shelfs.io.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Simple class for accessing resource files.
 */
@SuppressWarnings("unused")
public class ResourceLoader {

    private Class aClass;

    /**
     * Will try to access the libraries resource folder.
     */
    public ResourceLoader() {
        this.aClass = getClass();
    }

    /**
     * Loads the resources from the specified class.
     *
     * @param aClass will return the resources for the class jar.
     */
    public ResourceLoader(Class aClass) {
        this.aClass = aClass;
    }

    /**
     * Will return the content of a resource file.
     *
     * @param path the resource file path.
     * @return the resource file URL.
     */
    public URL getResourceFile(String path) {
        ClassLoader classLoader = this.aClass.getClassLoader();
        return classLoader.getResource(path);
    }

    /**
     * Will read out the file and return the content.
     *
     * @param path target file path.
     * @return file content as String.
     */
    public String getResourceFileContent(String path) {
        ClassLoader classLoader = this.aClass.getClassLoader();
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = classLoader.getResourceAsStream(path);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            bufferedReader.lines().forEach(stringBuilder::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * Will return the input stream of the given file path.
     *
     * @param path to the file in the resource directory.
     * @return the input stream of the File.
     */
    public InputStream getResourceFileInputStream(String path) {
        ClassLoader classLoader = this.aClass.getClassLoader();
        return classLoader.getResourceAsStream(path);
    }

    /**
     * Will check if the given resource exists/is accessible.
     *
     * @param path to the file.
     * @return true if the file exists and false if not or is not accessible.
     */
    public boolean doesResourceExist(String path) {
        try {
            URL url = this.aClass.getClassLoader().getResource(path);
            return url != null;
        } catch (Exception ignore) {
            return false;
        }
    }
}