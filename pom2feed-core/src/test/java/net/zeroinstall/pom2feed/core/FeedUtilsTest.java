package net.zeroinstall.pom2feed.core;

import org.junit.*;
import static org.junit.Assert.*;

public class FeedUtilsTest {

    @Test
    public void testPom2feedVersion() {
        assertEquals("1.0", FeedUtils.pom2feedVersion("1.0"));
    }

    @Test
    public void testGetSha1ManifestDigest() {
        assertEquals(
                "67600f59b06e4a3857e696f165d9dae02dc8a772",
                FeedUtils.getSha1ManifestDigest("123abc", 1024, "filename.jar"));
    }
}
