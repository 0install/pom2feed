package net.zeroinstall.pom2feed.core;

import com.google.common.base.Strings;
import java.net.URI;
import net.zeroinstall.model.*;
import org.apache.maven.model.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Iteratively builds Zero Install feeds using data from Maven projects.
 */
public class FeedBuilder {

    private final URI pom2feedService;
    private final InterfaceDocument document;
    private final Feed feed;

    /**
     * Creates feed builder for a new feed.
     *
     * @param pom2feedService The base URI of the pom2feed service used to
     * provide dependencies.
     */
    public FeedBuilder(URI pom2feedService) {
        this.pom2feedService = pom2feedService;
        this.document = InterfaceDocument.Factory.newInstance();
        this.feed = document.addNewInterface();
    }

    /**
     * Creates a feed builder an existing feed.
     *
     * @param pom2feedService The base URI of the pom2feed service used to
     * provide dependencies.
     * @param document The existing feed document.
     */
    public FeedBuilder(URI pom2feedService, InterfaceDocument document) {
        this.pom2feedService = pom2feedService;
        this.document = document;
        this.feed = (document.getInterface() != null) ? document.getInterface() : document.addNewInterface();
    }

    /**
     * Returns the generated feed/interface.
     *
     * @return An XML representation of the feed/interface.
     */
    public InterfaceDocument getDocument() {
        return document;
    }

    /**
     * Fills the feed with project-wide metadata from a Maven model.
     *
     * @param model The Maven model to extract the metadata from. Should be from
     * the latest version of the project.
     * @return The {@link FeedBuilder} instance for calling further methods in a
     * fluent fashion.
     */
    public FeedBuilder addMetadata(Model model) {
        feed.addName(model.getName());
        feed.addNewSummary().setStringValue("Auto-generated feed for " + model.getGroupId() + "." + model.getArtifactId());
        if (!Strings.isNullOrEmpty(model.getDescription())) {
            feed.addNewDescription().setStringValue(model.getDescription());
        }
        if (!Strings.isNullOrEmpty(model.getUrl())) {
            feed.addHomepage(model.getUrl());
        }
        return this;
    }

    /**
     * Adds a local-path implementation to the feed using version and dependency
     * information from a Maven model.
     *
     * @param model The Maven model to extract the version and dependency
     * information from.
     * @return The {@link FeedBuilder} instance for calling further methods in a
     * fluent fashion.
     */
    public FeedBuilder addLocalImplementation(Model model) {
        Implementation implementation = addImplementation(model);
        implementation.setLocalPath(".");
        return this;
    }

    /**
     * Adds a "download single file" implementation to the feed using version
     * and dependency information from a Maven model.
     *
     * @param model The Maven model to extract the version and dependency
     * information from.
     * @return The {@link FeedBuilder} instance for calling further methods in a
     * fluent fashion.
     */
    public FeedBuilder addRemoteImplementation(Model model, URI jarUri) {
        Implementation implementation = addImplementation(model);

        String hash = "abc";
        long size = 123;
        String fileName = model.getBuild().getFinalName() + "." + model.getPackaging();
        ManifestDigest digest = implementation.addNewManifestDigest();
        digest.setSha1New(FeedUtils.getSha1ManifestDigest(hash, size, fileName));

        // TODO: Add <file>

        return this;
    }

    /**
     * Adds an implementation to the feed using version and dependency
     * information from a Maven model.
     *
     * @param model The Maven model to extract the version and dependency
     * information from.
     * @return The implementation that was created and added to the feed.
     */
    private Implementation addImplementation(Model model) {
        Implementation implementation = feed.addNewImplementation();
        implementation.setVersion(FeedUtils.pom2feedVersion(model.getVersion()));
        if (!model.getLicenses().isEmpty()) {
            implementation.setLicense(model.getLicenses().get(0).getName());
        }

        addCommand(implementation, model);
        addDependencies(implementation, model);

        return implementation;
    }

    private void addCommand(Implementation implementation, Model model) {
        Command command = implementation.addNewCommand();
        command.setName("run");
        command.setPath(model.getBuild().getFinalName() + "." + model.getPackaging());

        Runner runner = command.addNewRunner();
        runner.setInterface("http://repo.roscidus.com/java/openjdk-jre");
        runner.addArg("-jar");
    }

    private void addDependencies(Implementation implementation, Model model) {
        Plugin compilerPlugin = model.getBuild().getPluginsAsMap().get("org.apache.maven.plugins:maven-compiler-plugin");
        if (compilerPlugin != null) {
            Xpp3Dom config = (Xpp3Dom) compilerPlugin.getConfiguration();
            String javaVersion = config.getChild("target").getValue();
            if (!Strings.isNullOrEmpty(javaVersion)) {
                net.zeroinstall.model.Dependency javaDep = implementation.addNewRequires();
                javaDep.setInterface("http://repo.roscidus.com/java/openjdk-jre");
                Constraint constraint = javaDep.addNewVersion2();
                constraint.setNotBefore(javaVersion);
            }
        }

        for (org.apache.maven.model.Dependency mavenDep : model.getDependencies()) {
            net.zeroinstall.model.Dependency ziDep = implementation.addNewRequires();
            ziDep.setInterface(pom2feedService.toString()
                    // TODO: Transform to proper feed URI
                    + mavenDep.getGroupId() + "/" + mavenDep.getArtifactId());
            ziDep.setVersion(FeedUtils.pom2feedVersion(mavenDep.getVersion()));

            Environment environment = ziDep.addNewEnvironment();
            environment.setName("CLASSPATH");
            environment.setInsert(".");
        }
    }
}
