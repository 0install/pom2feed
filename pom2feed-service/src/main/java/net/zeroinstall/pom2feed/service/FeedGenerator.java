package net.zeroinstall.pom2feed.service;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import net.zeroinstall.model.InterfaceDocument;
import net.zeroinstall.pom2feed.core.FeedBuilder;
import net.zeroinstall.pom2feed.core.MavenMetadata;
import net.zeroinstall.pom2feed.core.MavenUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.SAXException;

/**
 * Generates Zero Install feeds for Maven artifacts on demand.
 */
public class FeedGenerator implements FeedProvider {

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
     * Creates a feed generator.
     *
     * @param mavenRepository The base URL of the Maven repository used to
     * provide binaries.
     * @param pom2feedService The base URL of the pom2feed service used to
     * provide dependencies. This is usually the URL of this service itself.
     */
    public FeedGenerator(URL mavenRepository, URL pom2feedService) throws MalformedURLException {
        this.mavenRepository = (mavenRepository.toString().endsWith("/"))
                ? mavenRepository
                : new URL(mavenRepository.toString() + "/");
        this.pom2feedService = (pom2feedService.toString().endsWith("/"))
                ? pom2feedService
                : new URL(pom2feedService.toString() + "/");
    }

    @Override
    public String getFeed(final String artifactPath) throws IOException, SAXException, ModelBuildingException {
        InterfaceDocument feed = buildFeed(
                MavenMetadata.load(new URL(mavenRepository.toString() + artifactPath + "maven-metadata.xml")));

        File tempFile = File.createTempFile("pom2feed-service", ".xml");
        try {
            feed.save(tempFile,
                    new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
            return Files.toString(tempFile, Charsets.UTF_8);
        } finally {
            tempFile.delete();
        }
    }

    private InterfaceDocument buildFeed(MavenMetadata metadata) throws ModelBuildingException, IOException {
        FeedBuilder feedBuilder = new FeedBuilder(mavenRepository, pom2feedService);

        feedBuilder.addMetadata(
                getModel(metadata.getGroupId(), metadata.getArtifactId(), metadata.getLatestVersion()));

        for (String version : metadata.getVersions()) {
            feedBuilder.addRemoteImplementation(
                    getModel(metadata.getGroupId(), metadata.getArtifactId(), version));
        }

        return feedBuilder.getDocument();
    }

    private Model getModel(String groupId, String artifactId, String version) throws ModelBuildingException {
        UrlModelSource modelSource = new UrlModelSource(MavenUtils.getArtifactFileUrl(mavenRepository,
                groupId, artifactId, version, "pom"));
        ModelBuildingRequest request = new DefaultModelBuildingRequest();
        request.setModelSource(modelSource);
        request.setModelResolver(new RepositoryModelResolver());

        return new DefaultModelBuilderFactory().newInstance().
                build(request).getEffectiveModel();
    }

    private class RepositoryModelResolver implements ModelResolver {

        @Override
        public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
            return new UrlModelSource(MavenUtils.getArtifactFileUrl(mavenRepository,
                    groupId, artifactId, version, "pom"));
        }

        @Override
        public void addRepository(Repository repository) throws InvalidRepositoryException {
        }

        @Override
        public ModelResolver newCopy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
