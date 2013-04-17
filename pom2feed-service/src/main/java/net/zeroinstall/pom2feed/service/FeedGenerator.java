package net.zeroinstall.pom2feed.service;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import static com.google.common.base.Strings.isNullOrEmpty;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import net.zeroinstall.model.InterfaceDocument;
import net.zeroinstall.pom2feed.core.FeedBuilder;
import net.zeroinstall.pom2feed.core.MavenMetadata;
import static net.zeroinstall.pom2feed.core.MavenUtils.*;
import static net.zeroinstall.pom2feed.core.UrlUtils.*;
import org.apache.maven.model.*;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.xmlbeans.XmlCursor;
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
     * The command to execute in order to sign a feed file.
     */
    private final String signingBinary;

    /**
     * Creates a feed generator.
     *
     * @param mavenRepository The base URL of the Maven repository used to
     * provide binaries.
     * @param pom2feedService The base URL of the pom2feed service used to
     * provide dependencies. This is usually the URL of this service itself.
     * @param signingBinaryThe command to execute in order to sign a feed file.
     */
    public FeedGenerator(URL mavenRepository, URL pom2feedService, String signingBinary) throws MalformedURLException {
        this.mavenRepository = ensureSlashEnd(mavenRepository);
        this.pom2feedService = ensureSlashEnd(pom2feedService);
        this.signingBinary = signingBinary;
    }

    @Override
    public String getFeed(final String artifactPath) throws IOException, SAXException, ModelBuildingException {
        MavenMetadata metadata = MavenMetadata.load(new URL(mavenRepository, artifactPath + "maven-metadata.xml"));
        InterfaceDocument feed = buildFeed(metadata);

        File tempFile = File.createTempFile("pom2feed-service", ".xml");
        try {
            saveFeed(feed, tempFile);
            signFeed(tempFile.getPath());
            return Files.toString(tempFile, Charsets.UTF_8);
        } finally {
            tempFile.delete();
        }
    }

    private InterfaceDocument buildFeed(MavenMetadata metadata) throws ModelBuildingException {
        FeedBuilder feedBuilder = new FeedBuilder(mavenRepository, pom2feedService);
        feedBuilder.addMetadata(getModel(metadata, metadata.getLatestVersion()));
        for (String version : metadata.getVersions()) {
            try {
                feedBuilder.addRemoteImplementation(getModel(metadata, version));
            } catch (ModelBuildingException ex) {
            }
        }
        return feedBuilder.getDocument();
    }

    private Model getModel(MavenMetadata metadata, String version) throws ModelBuildingException {
        UrlModelSource modelSource = new UrlModelSource(getArtifactFileUrl(mavenRepository,
                metadata.getGroupId(), metadata.getArtifactId(), version, "pom"));
        ModelBuildingRequest request = new DefaultModelBuildingRequest();
        request.setModelSource(modelSource);
        request.setModelResolver(new RepositoryModelResolver());

        return new DefaultModelBuilderFactory().newInstance().
                build(request).getEffectiveModel();
    }

    private void saveFeed(InterfaceDocument feed, File tempFile) throws IOException {
        // Add XSL stylesheet reference
        XmlCursor cursor = feed.newCursor();
        cursor.toNextToken();
        cursor.insertProcInst("xml-stylesheet", "type='text/xsl' href='interface.xsl'");

        feed.save(tempFile, new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
    }

    private class RepositoryModelResolver implements ModelResolver {

        @Override
        public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
            return new UrlModelSource(getArtifactFileUrl(mavenRepository,
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

    private void signFeed(String path) throws IOException {
        if (isNullOrEmpty(signingBinary)) {
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(signingBinary, new String[]{path});

        try {
            if (process.waitFor() != 0) {
                throw new IOException("Unable to sign feed:\n"
                        + new Scanner(process.getErrorStream()).useDelimiter("\\A").next());
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }
}
