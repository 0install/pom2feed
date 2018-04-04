package net.zeroinstall.pom2feed.service;

import java.net.MalformedURLException;
import java.net.URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import net.zeroinstall.model.Feed;
import net.zeroinstall.model.InterfaceDocument;
import net.zeroinstall.publish.OpenPgp;

/**
 * Contains test cases run against live/online Maven repository to test special
 * case handling.
 */
@Ignore("Communicates over the internet.")
public class FeedGeneratorOnlineTest {

    private FeedGenerator feedGenerator;

    @Before
    public void before() throws MalformedURLException {
        this.feedGenerator = new FeedGenerator(mock(OpenPgp.class),
                new URL("http://repo.maven.apache.org/maven2/"),
                new URL("http://maven.0install.net/"),
                null);
    }

    @Test
    public void testAsm() throws Exception {
        Feed feed = InterfaceDocument.Factory.parse(feedGenerator.getFeed("asm/asm/")).getInterface();
        assertThat("ASM Core", equalTo(feed.getNameArray(0)));
    }

    @Test
    public void testXom() throws Exception {
        Feed feed = InterfaceDocument.Factory.parse(feedGenerator.getFeed("xom/xom/")).getInterface();
        assertThat("XOM", equalTo(feed.getNameArray(0)));
    }

    @Test
    public void testXmlResolver() throws Exception {
        Feed feed = InterfaceDocument.Factory.parse(feedGenerator.getFeed("xml-resolver/xml-resolver/")).getInterface();
        assertThat(feed.getImplementationArray().length, is(greaterThan(0)));
        assertThat("XML Commons Resolver Component", equalTo(feed.getNameArray(0)));
    }

    @Test
    public void testApacheCommonsLang3() throws Exception {
        Feed feed = InterfaceDocument.Factory.parse(feedGenerator.getFeed("org/apache/commons/commons-lang3/")).getInterface();
        assertThat("Commons Lang", equalTo(feed.getNameArray(0)));
    }

    @Test
    public void testApacheLdapApi() throws Exception {
        Feed feed = InterfaceDocument.Factory.parse(feedGenerator.getFeed("org/apache/directory/api/api-ldap-model/")).getInterface();
        assertThat(feed.getImplementationArray().length, is(greaterThan(0)));
        assertThat("Apache Directory LDAP API Model", equalTo(feed.getNameArray(0)));
    }

    @Test
    public void testGoogleGuava() throws Exception {
        Feed feed = InterfaceDocument.Factory.parse(feedGenerator.getFeed("com/google/guava/guava/")).getInterface();
        assertThat(feed.getImplementationArray().length, is(greaterThan(0)));
        assertThat("Guava: Google Core Libraries for Java", equalTo(feed.getNameArray(0)));
    }
}
