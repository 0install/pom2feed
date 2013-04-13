package net.zeroinstall.pom2feed.core;

import java.net.URI;
import net.zeroinstall.model.*;
import org.apache.maven.model.*;
import org.junit.*;
import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import static net.zeroinstall.pom2feed.core.FeedUtils.getSha1ManifestDigest;

public class FeedBuilderTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);
    private FeedBuilder builder = new FeedBuilder(
            URI.create("http://localhost:8089/"),
            URI.create("http://0install.de/maven/"));

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

        assertEquals(".", impl.getId());
        assertEquals(".", impl.getLocalPath());
        assertEquals("1.0", impl.getVersion());
        assertEquals("artifact.jar", impl.getCommandArray(0).getPath());
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
        assertEquals("sha1new=" + expectedDigest, impl.getId());
        assertEquals(expectedDigest, impl.getManifestDigestArray(0).getSha1New());
        assertEquals("1.0", impl.getVersion());
        assertEquals("artifact-1.0.jar", impl.getCommandArray(0).getPath());
    }
}
