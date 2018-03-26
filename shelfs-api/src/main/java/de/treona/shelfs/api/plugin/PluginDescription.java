package de.treona.shelfs.api.plugin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Version 1.0
 */
@SuppressWarnings("FieldCanBeLocal")
public class PluginDescription implements Serializable {


    private final static long serialVersionUID = -6314408650344698744L;
    /**
     * The Author Schema
     * <p>
     * The name of the author
     * (Required)
     */
    @SerializedName("author")
    @Expose
    private String author = "Unknown-Author";
    /**
     * The Name Schema
     * <p>
     * The name of the plugin
     * (Required)
     */
    @SerializedName("name")
    @Expose
    private String name = "Unknown-Plugin";
    /**
     * The Version Schema
     * <p>
     * The version of the plugin
     * (Required)
     */
    @SerializedName("version")
    @Expose
    private String version = "1.0.0";
    /**
     * The Main Schema
     * <p>
     * The path to the main class inside the plugin jar
     * (Required)
     */
    @SerializedName("main")
    @Expose
    private String main = "Main";
    @SerializedName("dependencies")
    @Expose
    private List<String> dependencies = null;

    /**
     * The Author Schema
     * <p>
     * The name of the author
     * (Required)
     */
    public String getAuthor() {
        return author;
    }

    /**
     * The Name Schema
     * <p>
     * The name of the plugin
     * (Required)
     */
    public String getName() {
        return name;
    }

    /**
     * The Version Schema
     * <p>
     * The version of the plugin
     * (Required)
     */
    public String getVersion() {
        return version;
    }

    /**
     * The Main Schema
     * <p>
     * The path to the main class inside the plugin jar
     * (Required)
     */
    public String getMain() {
        return main;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

}