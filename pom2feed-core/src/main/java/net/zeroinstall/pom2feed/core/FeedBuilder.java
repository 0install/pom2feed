package net.zeroinstall.pom2feed.core;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import java.net.URI;
import net.zeroinstall.model.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.maven.model.*;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

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

    public InterfaceDocument generateFeed(Model model) throws XmlException {
        InterfaceDocument document = InterfaceDocument.Factory.newInstance();
        Feed feed = document.addNewInterface();

        feed.addName(model.getName());
        feed.addNewSummary().set(plainText(
                Strings.isNullOrEmpty(model.getDescription())
                ? "Maven artifact"
                : model.getDescription()));
        if (!Strings.isNullOrEmpty(model.getUrl())) {
            feed.addHomepage(model.getUrl());
        }

        // TODO: Convert more stuff

        return document;
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
        impl.setVersion(pom2feedVersion(model.getVersion()));

        for (org.apache.maven.model.Dependency mavenDep : model.getDependencies()) {
            net.zeroinstall.model.Dependency ziDep = impl.addNewRequires();
            ziDep.setInterface(pom2feedService.toString()
                    // TODO: Transform to proper feed URI
                    + mavenDep.getGroupId() + "/" + mavenDep.getArtifactId());
            ziDep.setVersion(pom2feedVersion(mavenDep.getVersion()));
        }

        return impl;
    }

    private static XmlObject plainText(String value) {
        try {
            return XmlObject.Factory.parse(StringEscapeUtils.escapeXml(value));
        } catch (XmlException ex) {
            throw Throwables.propagate(ex);
        }
    }

    private static String pom2feedVersion(String pomVersion) {
        // TODO: Handle -snapshot, rc, etc.
        return pomVersion;
    }
}
