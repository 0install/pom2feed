package net.zeroinstall.pom2feed.core;

import java.net.URL;
import net.zeroinstall.model.*;
import org.apache.maven.model.*;
import org.junit.*;
import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.net.MalformedURLException;
import static net.zeroinstall.pom2feed.core.ManifestUtils.*;

public class FeedBuilderTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);
    private FeedBuilder builder;

    @Before
    public void before() throws MalformedURLException {
        this.builder = new FeedBuilder(
                new URL("http://localhost:8089/"),
                new URL("http://maven.0install.net/"));
    }

    @Test
    public void testAddMetadata() {
        Model model = new Model();
        model.setName("Name");
        model.setDescription("Description");

        Feed feed = builder.addMetadata(model).
                getDocument().getInterface();

        assertEquals("Name", feed.getNameArray(0));
        assertEquals("Description", feed.getDescriptionArray(0).getStringValue());
    }

    @Test
    public void testAddLocalImplementation() {
        Model model = new Model();
        Build build = new Build();
        build.setFinalName("artifact");
        model.setBuild(build);
        model.setPackaging("jar");
        model.setVersion("1.0");

        Implementation impl = builder.addLocalImplementation(model, "dir").
                getDocument().getInterface().getImplementationArray(0);

        assertEquals("1.0", impl.getId());
        assertEquals("1.0", impl.getVersion());
        assertEquals("dir", impl.getLocalPath());
        assertEquals("artifact.jar", impl.getCommandArray(0).getPath());
        assertEquals("CLASSPATH", impl.getEnvironmentArray(0).getName());
        assertEquals("artifact.jar", impl.getEnvironmentArray(0).getInsert());
    }

    @Test
    public void testAddLocalImplementationNonJar() {
        Model model = new Model();
        Build build = new Build();
        build.setFinalName("artifact");
        model.setBuild(build);
        model.setPackaging("war");
        model.setVersion("1.0");

        Implementation impl = builder.addLocalImplementation(model, "dir").
                getDocument().getInterface().getImplementationArray(0);

        assertEquals("1.0", impl.getId());
        assertEquals("1.0", impl.getVersion());
        assertEquals("dir", impl.getLocalPath());
        assertEquals(0, impl.getCommandArray().length); // No command for non-JAR
    }

    @Test
    public void testAddLocalDependencies() {
        Model model = new Model();
        Build build = new Build();
        model.setBuild(build);
        build.setFinalName("artifact");
        model.setPackaging("jar");
        model.setVersion("1.0");

        org.apache.maven.model.Dependency compileDependency = new org.apache.maven.model.Dependency();
        compileDependency.setGroupId("dependency-group");
        compileDependency.setArtifactId("dependency-artifact-compile");
        compileDependency.setVersion("[2.0,3.0)");
        compileDependency.setScope("compile");
        model.addDependency(compileDependency);

        org.apache.maven.model.Dependency providedDependency = new org.apache.maven.model.Dependency();
        providedDependency.setGroupId("dependency-group");
        providedDependency.setArtifactId("dependency-artifact-provided");
        providedDependency.setVersion("[2.0,3.0)");
        providedDependency.setScope("provided");
        model.addDependency(providedDependency);

        org.apache.maven.model.Dependency runtimeDependency = new org.apache.maven.model.Dependency();
        runtimeDependency.setGroupId("dependency-group");
        runtimeDependency.setArtifactId("dependency-artifact-runtime");
        runtimeDependency.setVersion("[2.0,3.0)");
        runtimeDependency.setScope("runtime");
        model.addDependency(runtimeDependency);

        org.apache.maven.model.Dependency testDependency = new org.apache.maven.model.Dependency();
        testDependency.setGroupId("dependency-group");
        testDependency.setArtifactId("dependency-artifact-test");
        testDependency.setVersion("[2.0,3.0)");
        testDependency.setScope("test");
        model.addDependency(testDependency);

        Implementation impl = builder.addLocalImplementation(model, "dir").
                getDocument().getInterface().getImplementationArray(0);

        assertEquals("http://maven.0install.net/dependency-group/dependency-artifact-compile/", impl.getRequiresArray(0).getInterface());
        assertEquals("2.0..!3.0", impl.getRequiresArray(0).getVersion());
        assertEquals("http://maven.0install.net/dependency-group/dependency-artifact-runtime/", impl.getRequiresArray(1).getInterface());
        assertEquals("2.0..!3.0", impl.getRequiresArray(1).getVersion());
        assertEquals(2, impl.getRequiresArray().length); // No requirement for test-only dependencies
    }

    @Test
    public void testAddRemoteImplementation() throws IOException {
        Model model = new Model();
        model.setGroupId("group");
        model.setArtifactId("artifact");
        model.setPackaging("jar");
        model.setVersion("1.0");
        stubFor(head(urlEqualTo("/group/artifact/1.0/artifact-1.0.jar")).
                willReturn(aResponse().withStatus(200).withHeader("Content-Length", "1024")));
        stubFor(get(urlEqualTo("/group/artifact/1.0/artifact-1.0.jar.sha1")).
                willReturn(aResponse().withStatus(200).withBody("123abc")));

        Implementation impl = builder.addRemoteImplementation(model).
                getDocument().getInterface().getImplementationArray(0);

        verify(headRequestedFor(urlEqualTo("/group/artifact/1.0/artifact-1.0.jar")));
        verify(getRequestedFor(urlEqualTo("/group/artifact/1.0/artifact-1.0.jar.sha1")));
        String expectedDigest = getSha1ManifestDigest("123abc", 1024, "artifact-1.0.jar");
        assertEquals("1.0", impl.getId());
        assertEquals("1.0", impl.getVersion());
        assertEquals(expectedDigest, impl.getManifestDigestArray(0).getSha1New());
        assertEquals("artifact-1.0.jar", impl.getCommandArray(0).getPath());
        assertEquals("CLASSPATH", impl.getEnvironmentArray(0).getName());
        assertEquals("artifact-1.0.jar", impl.getEnvironmentArray(0).getInsert());
    }

    @Test
    public void testAddRemoteImplementationNonJar() throws IOException {
        Model model = new Model();
        model.setGroupId("group");
        model.setArtifactId("artifact");
        model.setPackaging("war");
        model.setVersion("1.0");
        stubFor(head(urlEqualTo("/group/artifact/1.0/artifact-1.0.war")).
                willReturn(aResponse().withStatus(200).withHeader("Content-Length", "1024")));
        stubFor(get(urlEqualTo("/group/artifact/1.0/artifact-1.0.war.sha1")).
                willReturn(aResponse().withStatus(200).withBody("123abc")));

        Implementation impl = builder.addRemoteImplementation(model).
                getDocument().getInterface().getImplementationArray(0);

        verify(headRequestedFor(urlEqualTo("/group/artifact/1.0/artifact-1.0.war")));
        verify(getRequestedFor(urlEqualTo("/group/artifact/1.0/artifact-1.0.war.sha1")));
        String expectedDigest = getSha1ManifestDigest("123abc", 1024, "artifact-1.0.war");
        assertEquals("1.0", impl.getId());
        assertEquals("1.0", impl.getVersion());
        assertEquals(expectedDigest, impl.getManifestDigestArray(0).getSha1New());
        assertEquals(0, impl.getCommandArray().length); // No command for non-JAR
    }

    @Test
    public void testAddRemoteImplementationMissing() {
        Model model = new Model();
        model.setGroupId("group");
        model.setArtifactId("artifact");
        model.setPackaging("war");
        model.setVersion("1.0");
        stubFor(head(urlEqualTo("/group/artifact/1.0/artifact-1.0.war")).
                willReturn(aResponse().withStatus(404)));
        stubFor(get(urlEqualTo("/group/artifact/1.0/artifact-1.0.war.sha1")).
                willReturn(aResponse().withStatus(404)));

        boolean thrown = false;
        try {
            builder.addRemoteImplementation(model);
        } catch (IOException ex) {
            thrown = true;
        }
        assertTrue(thrown);
        Feed feed = builder.getDocument().getInterface();

        verify(headRequestedFor(urlEqualTo("/group/artifact/1.0/artifact-1.0.war")));
        verify(getRequestedFor(urlEqualTo("/group/artifact/1.0/artifact-1.0.war.sha1")));
        assertEquals(0, feed.getImplementationArray().length);
    }
    
    
    @Test
    public void testLaxVersions() {
        Model model = new Model();
        Build build = new Build();
        model.setBuild(build);
        build.setFinalName("artifact");
        model.setPackaging("jar");
        model.setVersion("1.0");

        org.apache.maven.model.Dependency compileDependency = new org.apache.maven.model.Dependency();
        compileDependency.setGroupId("dependency-group");
        compileDependency.setArtifactId("dependency-artifact-compile");
        compileDependency.setVersion("2.0");
        compileDependency.setScope("compile");
        model.addDependency(compileDependency);

        Implementation impl = builder.enableLaxDependencyVersions().addLocalImplementation(model, "dir").
                getDocument().getInterface().getImplementationArray(0);
        assertEquals("2.0..", impl.getRequiresArray(0).getVersion());
    }
}
