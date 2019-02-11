package net.zeroinstall.pom2feed.service;

import static com.google.common.collect.Lists.newArrayList;
import java.io.IOException;
import java.net.URL;
import javax.xml.xpath.XPathExpressionException;
import net.zeroinstall.model.InterfaceDocument;
import net.zeroinstall.pom2feed.core.*;
import static net.zeroinstall.pom2feed.core.MavenUtils.*;
import static net.zeroinstall.pom2feed.core.UrlUtils.*;
import static net.zeroinstall.publish.FeedUtils.getFeedString;
import org.apache.maven.model.*;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.*;
import org.slf4j.*;
import org.xml.sax.SAXException;

/**
 * Generates Zero Install feeds for Maven artifacts on demand.
 */
public class FeedGenerator implements FeedProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedGenerator.class);
    /**
     * The base URL of the Maven repository used to provide binaries.
     */
    private final URL mavenRepository;
    /**
     * The base URL of the pom2feed service used to provide dependencies. This
     * is usually the URL of this service itself.
     */
    private final URL pom2feedService;
    /**
     * The name of the key to use for GnuPG signing.
     */
    private final String gnuPGKey;

    /**
     * Creates a feed generator.
     *
     * @param mavenRepository The base URL of the Maven repository used to
     * provide binaries.
     * @param pom2feedService The base URL of the pom2feed service used to
     * provide dependencies. This is usually the URL of this service itself.
     * @param gnuPGKey The name of the key to use for GnuPG signing.
     */
    public FeedGenerator(URL mavenRepository, URL pom2feedService, String gnuPGKey) {
        this.mavenRepository = ensureSlashEnd(mavenRepository);
        this.pom2feedService = ensureSlashEnd(pom2feedService);
        this.gnuPGKey = gnuPGKey;
    }

    @Override
    public String getFeed(final String artifactPath) throws IOException, SAXException, XPathExpressionException, ModelBuildingException {
        MavenMetadata metadata = getMetadata(artifactPath);
        InterfaceDocument feed = buildFeed(metadata);
        return getFeedString(feed, gnuPGKey);
    }

    private MavenMetadata getMetadata(String artifactPath) throws IOException, SAXException, XPathExpressionException {
        if (mavenRepository.getHost().equals("repo.maven.apache.org")) {
            return MavenMetadata.query(new URL("http://search.maven.org/solrsearch/"), artifactPath);
        } else {
            return MavenMetadata.load(new URL(mavenRepository, artifactPath + "maven-metadata.xml"));
        }
    }

    private InterfaceDocument buildFeed(MavenMetadata metadata) throws ModelBuildingException {
        FeedBuilder feedBuilder = new FeedBuilder(mavenRepository, pom2feedService).enableLaxDependencyVersions();
        addMetadataToFeed(metadata, feedBuilder);
        addImplementationsToFeed(metadata, feedBuilder);

        InterfaceDocument feed = feedBuilder.getDocument();
        feed.getInterface().setUri(getServiceUrl(pom2feedService, metadata.getGroupId(), metadata.getArtifactId()));
        return feed;
    }

    private void addMetadataToFeed(MavenMetadata metadata, FeedBuilder feedBuilder) throws ModelBuildingException {
        Model model;
        try {
            model = getModel(metadata, metadata.getLatestVersion());
        } catch (ModelBuildingException ex) {
            // Fall back to first version if latest version does not work
            model = getModel(metadata, metadata.getVersions().get(0));
        }

        feedBuilder.addMetadata(model);
    }

    private void addImplementationsToFeed(MavenMetadata metadata, FeedBuilder feedBuilder) {
        for (String version : metadata.getVersions()) {
            try {
                feedBuilder.addRemoteImplementation(getModel(metadata, version));
            } catch (ModelBuildingException ex) {
                LOGGER.trace(null, ex);
            } catch (IOException ex) {
                LOGGER.trace(null, ex);
            } catch (IllegalArgumentException ex) {
                LOGGER.trace(null, ex);
            }
        }
    }

    private Model getModel(MavenMetadata metadata, String version) throws ModelBuildingException {
        UrlModelSource modelSource = new UrlModelSource(getArtifactFileUrl(mavenRepository,
                metadata.getGroupId(), metadata.getArtifactId(), version, "pom"));
        ModelBuildingRequest request = new DefaultModelBuildingRequest()
                .setModelSource(modelSource)
                .setModelResolver(new RepositoryModelResolver())
                .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
                // HACK: Workaround for "Failed to determine Java version for profile XYZ"
                .setInactiveProfileIds(newArrayList("java-1.5-detected", "jdk7", "jdk8"));

        ModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
        return builder.build(request).getEffectiveModel();
    }

    private class RepositoryModelResolver implements ModelResolver {

        @Override
        public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
            return new UrlModelSource(getArtifactFileUrl(mavenRepository,
                    groupId, artifactId, version, "pom"));
        }

        @Override
        public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
            return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
        }

        @Override
        public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
            return resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
        }

        @Override
        public void addRepository(Repository repository) {
        }

        @Override
        public void addRepository(Repository rpstr, boolean bln) {
        }

        @Override
        public ModelResolver newCopy() {
            return new RepositoryModelResolver();
        }
    }
}
