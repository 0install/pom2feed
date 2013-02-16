package net.zeroinstall.pom2feed.core;

import java.net.URI;
import net.zeroinstall.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.*;
import static org.apache.xmlbeans.XmlObject.Factory.newValue;

/**
 * Builds Zero Install feeds using data from Maven projects.
 */
public class FeedBuilder {

    private final URI pom2feedService;

    /**
     * Creates a new feed builder.
     *
     * @param pom2feedService The base URI of the pom2feed service used to
     * provide dependencies.
     */
    public FeedBuilder(URI pom2feedService) {
        this.pom2feedService = pom2feedService;
    }

    public Feed generateFeed(Model model) {
        Feed feed = Feed.Factory.newInstance();
        feed.addName(model.getName());

        feed.addNewSummary().set(newValue(
                StringUtils.isEmpty(model.getDescription())
                ? "Maven artifact"
                : model.getDescription()));
        if (!StringUtils.isEmpty(model.getUrl())) {
            feed.addHomepage(model.getUrl());
        }

        // TODO: Convert more stuff

        return feed;
    }

    public Implementation addLocalImplementation(Feed feed, Model model) {
        Implementation impl = addImplementation(feed, model);
        impl.setLocalPath(".");
        return impl;
    }

    public Implementation addRemoteImplementation(Feed feed, Model model, URI jarUri) {
        Implementation impl = addImplementation(feed, model);
        // TODO: Add <file>
        return impl;
    }

    private Implementation addImplementation(Feed feed, Model model) {
        Implementation impl = feed.addNewImplementation();

        // TODO: Convert Maven version numbers to Zero Install version numbers
        impl.setVersion(model.getVersion());

        for (org.apache.maven.model.Dependency dependency : model.getDependencies()) {
            net.zeroinstall.model.Dependency dep = impl.addNewRequires();
            dep.setInterface(pom2feedService.toString()
                    // TODO: Transform to proper feed URI
                    + dependency.getGroupId() + "/" + dependency.getArtifactId());
        }

        return impl;
    }
}
