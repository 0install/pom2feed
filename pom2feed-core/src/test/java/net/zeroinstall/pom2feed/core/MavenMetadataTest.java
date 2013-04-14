package net.zeroinstall.pom2feed.core;

import java.net.URL;
import org.junit.*;
import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import static com.google.common.collect.Lists.newArrayList;

public class MavenMetadataTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void testLoad() throws Exception {
        stubFor(get(urlEqualTo("/maven-metadata.xml")).willReturn(aResponse().withStatus(200).
                withBody("<metadata>\n<groupId>group</groupId>\n<artifactId>artifact</artifactId>\n<versioning>\n<latest>1.2</latest>\n<versions>\n<version>1.0</version>\n<version>1.2</version>\n<version>1.1</version>\n</versions>\n</versioning>\n</metadata>")));

        MavenMetadata metadata = MavenMetadata.load(new URL("http://localhost:8089/maven-metadata.xml"));

        verify(getRequestedFor(urlEqualTo("/maven-metadata.xml")));
        assertEquals("group", metadata.getGroupId());
        assertEquals("artifact", metadata.getArtifactId());
        assertEquals("1.2", metadata.getLatestVersion());
        assertEquals(newArrayList("1.0", "1.2", "1.1"), metadata.getVersions());
    }

    @Test
    public void testLoadMissingLatest() throws Exception {
        stubFor(get(urlEqualTo("/maven-metadata.xml")).willReturn(aResponse().withStatus(200).
                withBody("<metadata>\n<groupId>group</groupId>\n<artifactId>artifact</artifactId>\n<versioning>\n<versions>\n<version>1.0</version>\n<version>1.1</version>\n<version>1.2</version>\n</versions>\n</versioning>\n</metadata>")));

        MavenMetadata metadata = MavenMetadata.load(new URL("http://localhost:8089/maven-metadata.xml"));

        verify(getRequestedFor(urlEqualTo("/maven-metadata.xml")));
        assertEquals("group", metadata.getGroupId());
        assertEquals("artifact", metadata.getArtifactId());
        assertEquals("1.2", metadata.getLatestVersion());
        assertEquals(newArrayList("1.0", "1.1", "1.2"), metadata.getVersions());
    }
}
