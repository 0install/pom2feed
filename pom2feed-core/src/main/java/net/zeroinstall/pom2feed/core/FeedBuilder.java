package net.zeroinstall.pom2feed.core;

import java.net.URI;
import net.zeroinstall.model.*;
import org.apache.maven.model.*;
import static org.apache.xmlbeans.XmlObject.Factory.newValue;

public class FeedBuilder {

    private final URI pom2feedService;

    public FeedBuilder(URI pom2feedService) {
        this.pom2feedService = pom2feedService;
    }

    public Feed generateFeed(Model model) {
        Feed feed = Feed.Factory.newInstance();
        feed.addName(model.getName());

        feed.addNewSummary().set(newValue(model.getDescription()));
        feed.addHomepage(model.getUrl());

        // TODO

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
