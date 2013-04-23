package net.zeroinstall.pom2feed.service;

import java.net.URL;
import org.junit.*;
import static org.junit.Assert.*;
import java.net.MalformedURLException;
import net.zeroinstall.model.Feed;
import net.zeroinstall.model.InterfaceDocument;

/**
 * Contains test cases run against live/online Maven repository to test special
 * case handling.
 */
@Ignore
public class FeedGeneratorOnlineTest {

    private FeedGenerator feedGenerator;

    @Before
    public void before() throws MalformedURLException {
        this.feedGenerator = new FeedGenerator(
                new URL("http://repo.maven.apache.org/maven2/"),
                new URL("http://maven.0install.net/"),
                null);
    }

    @Test
    public void testApacheCommonsLang3() throws Exception {
        Feed feed = InterfaceDocument.Factory.parse(feedGenerator.getFeed("org/apache/commons/commons-lang3/")).getInterface();

        assertEquals("Commons Lang", feed.getNameArray(0));
    }
}
