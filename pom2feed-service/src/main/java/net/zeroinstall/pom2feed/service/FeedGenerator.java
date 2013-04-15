package net.zeroinstall.pom2feed.service;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import static com.google.common.base.Strings.isNullOrEmpty;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
        this.mavenRepository = (mavenRepository.toString().endsWith("/"))
                ? mavenRepository
                : new URL(mavenRepository.toString() + "/");
        this.pom2feedService = (pom2feedService.toString().endsWith("/"))
                ? pom2feedService
                : new URL(pom2feedService.toString() + "/");
        this.signingBinary = signingBinary;
    }
    
    @Override
    public String getFeed(final String artifactPath) throws IOException, SAXException, ModelBuildingException {
        InterfaceDocument feed = buildFeed(
                MavenMetadata.load(new URL(mavenRepository, artifactPath + "maven-metadata.xml")));
        
        File tempFile = File.createTempFile("pom2feed-service", ".xml");
        try {
            feed.save(tempFile, new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
            signFeed(tempFile.getPath());
            return Files.toString(tempFile, Charsets.UTF_8);
        } finally {
            tempFile.delete();
        }
    }
    
    private InterfaceDocument buildFeed(MavenMetadata metadata) throws ModelBuildingException, IOException {
        FeedBuilder feedBuilder = new FeedBuilder(mavenRepository, pom2feedService);
        
        feedBuilder.addMetadata(
                getModel(metadata, metadata.getLatestVersion()));
        
        for (String version : metadata.getVersions()) {
            feedBuilder.addRemoteImplementation(
                    getModel(metadata, version));
        }
        
        return feedBuilder.getDocument();
    }
    
    private Model getModel(MavenMetadata metadata, String version) throws ModelBuildingException {
        UrlModelSource modelSource = new UrlModelSource(MavenUtils.getArtifactFileUrl(mavenRepository,
                metadata.getGroupId(), metadata.getArtifactId(), version, "pom"));
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
    
    private void signFeed(String path) throws IOException {
        if (isNullOrEmpty(signingBinary)) {
            return;
        }
        
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(signingBinary, new String[]{path});
        
        try {
            if (process.waitFor() != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                throw new IOException("Unable to sign feed:\n" + errorReader.readLine());
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }
}
