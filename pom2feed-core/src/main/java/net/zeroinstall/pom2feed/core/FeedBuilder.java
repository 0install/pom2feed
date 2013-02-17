package net.zeroinstall.pom2feed.core;

import com.google.common.base.Strings;
import java.net.URI;
import net.zeroinstall.model.*;
import org.apache.maven.model.*;

/**
 * Iterativley builds Zero Install feeds using data from Maven projects.
 */
public class FeedBuilder {

    private final URI pom2feedService;
    private final InterfaceDocument document = InterfaceDocument.Factory.newInstance();
    private final Feed feed = document.addNewInterface();

    /**
     * Creates a new feed builder.
     *
     * @param pom2feedService The base URI of the pom2feed service used to
     * provide dependencies.
     */
    public FeedBuilder(URI pom2feedService) {
        this.pom2feedService = pom2feedService;
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
     */
    public void addMetadata(Model model) {
        feed.addName(model.getName());
        feed.addNewSummary().setStringValue("Auto-generated feed for " + model.getGroupId() + "." + model.getArtifactId());
        if (!Strings.isNullOrEmpty(model.getDescription())) {
            feed.addNewDescription().setStringValue(model.getDescription());
        }
        if (!Strings.isNullOrEmpty(model.getUrl())) {
            feed.addHomepage(model.getUrl());
        }
    }

    /**
     * Adds a local-path implementation to the feed using version and dependency
     * information from a Maven model.
     *
     * @param model The Maven model to extract the version and dependency
     * information from.
     * @return The implementation that was created and added to the feed.
     */
    public Implementation addLocalImplementation(Model model) {
        Implementation impl = addImplementation(model);
        impl.setLocalPath(".");
        return impl;
    }

    /**
     * Adds a "download single file" implementation to the feed using version
     * and dependency information from a Maven model.
     *
     * @param model The Maven model to extract the version and dependency
     * information from.
     * @return The implementation that was created and added to the feed.
     */
    public Implementation addRemoteImplementation(Model model, URI jarUri) {
        Implementation impl = addImplementation(model);

        String hash = "abc";
        long size = 123;
        String fileName = model.getBuild().getFinalName() + "." + model.getPackaging();
        ManifestDigest digest = impl.addNewManifestDigest();
        digest.setSha1New(FeedUtils.getSha1ManifestDigest(hash, size, fileName));

        // TODO: Add <file>

        return impl;
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
        Implementation impl = feed.addNewImplementation();
        impl.setVersion(FeedUtils.pom2feedVersion(model.getVersion()));

        if (!model.getLicenses().isEmpty()) {
            impl.setLicense(model.getLicenses().get(0).getName());
        }

        Command command = impl.addNewCommand();
        command.setName("run");
        command.setPath(model.getBuild().getFinalName() + "." + model.getPackaging());

        for (org.apache.maven.model.Dependency mavenDep : model.getDependencies()) {
            net.zeroinstall.model.Dependency ziDep = impl.addNewRequires();
            ziDep.setInterface(pom2feedService.toString()
                    // TODO: Transform to proper feed URI
                    + mavenDep.getGroupId() + "/" + mavenDep.getArtifactId());
            ziDep.setVersion(FeedUtils.pom2feedVersion(mavenDep.getVersion()));
        }

        return impl;
    }
}
