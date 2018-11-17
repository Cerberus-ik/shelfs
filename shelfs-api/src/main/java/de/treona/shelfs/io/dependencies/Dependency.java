package de.treona.shelfs.io.dependencies;


import com.google.common.base.Objects;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Core component of this Util
 * <p>
 * <p>Holds details of the Maven Dependency</p>
 */
@SuppressWarnings("WeakerAccess")
public final class Dependency {

    private final String name, version, group, artifact;

    private Dependency parent = null;

    /**
     * Create a new Dependency
     *
     * @param name       The name of the Dependency, can be anything, but recommended to be something global
     * @param version    The version of this Maven Artifact
     * @param group    The group of this Maven Artifact
     * @param artifact The artifact of this Maven Artifact
     * @param customRepo The custom repository to load this from
     */
    public Dependency(String name, String version, String group, String artifact, String customRepo, boolean alwaysUpdate) {
        this.name = name;
        this.version = version;
        this.group = group;
        this.artifact = artifact;
    }

    /**
     * @see #Dependency(String, String, String, String, String, boolean)
     */
    public Dependency(String name, String version, String group, String artifact) {
        this(name, version, group, artifact, "", false);
    }

    /**
     * The name of this Dependency
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * The Maven Version of this Dependency
     *
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * The Maven Group ID of this Dependency
     *
     * @return The group
     */
    public String getGroup() {
        return group;
    }

    /**
     * The Maven Artifact ID of this Dependency
     *
     * @return The artifact
     */
    public String getArtifact() {
        return artifact;
    }


    /**
     * The Parent Dependency, meaning The parent depends on this
     *
     * @return The parent
     */
    public Dependency getParent() {
        return parent;
    }

    /**
     * Set the parent Dependency
     *
     * @param parent The new parent
     */
    public void setParent(Dependency parent) {
        this.parent = parent;
    }

    /**
     * Check if this Dependency has a parent
     *
     * @return true if it does, false otherwise
     */
    public boolean hasParent() {
        return getParent() != null;
    }

    /**
     * Get the depth of this dependency
     * <p>
     * <p>Where "this" is this dependency, and "Parent" is it's parent</p>
     * <p>
     * <p>this, will return 0</p>
     * <p>Parent > this, will return 1</p>
     * <p>Parent > Parent > this, will return 2</p>
     *
     * @return The depth of this Dependency's parents
     */
    public int getParentDepth() {
        int depth = 0;

        Dependency parent = getParent();
        while (parent != null) {
            parent = parent.getParent();
            depth++;
        }

        return depth;
    }


    /**
     * The name of this Dependency's Jar file in the Repo
     *
     * @return The jar name
     */
    public String getJarName() {
        return getArtifact() + "-" + getVersion() + ".jar";
    }

    /**
     * The name of this Dependency's POM file in the Repo
     *
     * @return The pom name
     */
    public String getPomName() {
        return getArtifact() + "-" + getVersion() + ".pom";
    }

    public URL getUrl() throws MalformedURLException {
        return new URL("http://central.maven.org/maven2/" + this.group.replaceAll("\\.", "/") + "/" + this.artifact + "/" + this.version + "/" + this.getJarName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dependency)) return false;
        Dependency that = (Dependency) o;
        return Objects.equal(getVersion(), that.getVersion()) &&
                Objects.equal(getGroup(), that.getGroup()) &&
                Objects.equal(getArtifact(), that.getArtifact());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getVersion(), getGroup(), getArtifact());
    }

}
