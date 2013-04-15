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
import static net.zeroinstall.pom2feed.core.FeedUtils.getSha1ManifestDigest;

public class FeedBuilderTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);
    private FeedBuilder builder;

    public FeedBuilderTest() throws MalformedURLException {
        this.builder = new FeedBuilder(
                new URL("http://localhost:8089/"),
                new URL("http://0install.de/maven/"));
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

        Implementation impl = builder.addLocalImplementation(model).
                getDocument().getInterface().getImplementationArray(0);

        assertEquals("1.0", impl.getId());
        assertEquals("1.0", impl.getVersion());
        assertEquals(".", impl.getLocalPath());
        assertEquals("artifact.jar", impl.getCommandArray(0).getPath());
    }

    @Test
    public void testAddLocalImplementationNonJar() {
        Model model = new Model();
        Build build = new Build();
        build.setFinalName("artifact");
        model.setBuild(build);
        model.setPackaging("war");
        model.setVersion("1.0");

        Implementation impl = builder.addLocalImplementation(model).
                getDocument().getInterface().getImplementationArray(0);

        assertEquals("1.0", impl.getId());
        assertEquals("1.0", impl.getVersion());
        assertEquals(".", impl.getLocalPath());
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

        org.apache.maven.model.Dependency dependency = new org.apache.maven.model.Dependency();
        dependency.setGroupId("dependency-group");
        dependency.setArtifactId("dependency-artifact");
        dependency.setVersion("2.0");
        dependency.setScope("compile");
        model.addDependency(dependency);

        org.apache.maven.model.Dependency testDependency = new org.apache.maven.model.Dependency();
        testDependency.setGroupId("dependency-group");
        testDependency.setArtifactId("dependency-artifact");
        testDependency.setVersion("2.0");
        testDependency.setScope("test");
        model.addDependency(testDependency);

        Implementation impl = builder.addLocalImplementation(model).
                getDocument().getInterface().getImplementationArray(0);

        assertEquals("http://0install.de/maven/dependency-group/dependency-artifact/", impl.getRequiresArray(0).getInterface());
        assertEquals("2.0", impl.getRequiresArray(0).getVersion());
        assertEquals(1, impl.getRequiresArray().length); // No requirement for test-only dependencies
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
}
