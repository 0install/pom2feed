package net.zeroinstall.pom2feed.core;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.*;
import static org.junit.Assert.*;
import static net.zeroinstall.pom2feed.core.UrlUtils.*;

public class UrlUtilsTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void testEnsureSlashEnd() throws MalformedURLException {
        assertEquals(new URL("http://localhost/test/"), ensureSlashEnd(new URL("http://localhost/test")));
        assertEquals(new URL("http://localhost/test/"), ensureSlashEnd(new URL("http://localhost/test/")));
    }

    @Test
    public void testGetRemoteFileSize() throws IOException {
        stubFor(head(urlEqualTo("/test")).
                willReturn(aResponse().withStatus(200).withHeader("Content-Length", "1024")));

        assertEquals(1024, getRemoteFileSize(new URL("http://localhost:8089/test")));

        verify(headRequestedFor(urlEqualTo("/test")));
    }

    @Test
    public void testGetRemoteLine() throws IOException {
        stubFor(get(urlEqualTo("/test")).
                willReturn(aResponse().withStatus(200).withBody("abc\n123")));

        assertEquals("abc", getRemoteLine(new URL("http://localhost:8089/test")));

        verify(getRequestedFor(urlEqualTo("/test")));
    }
}
