package net.zeroinstall.pom2feed.service;

import static com.google.common.collect.Lists.newArrayList;
import java.util.Collection;
import static net.zeroinstall.pom2feed.service.ArtifactUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ArtifactUtilsTest {

    private final String url;
    private final boolean expectedResult;

    public ArtifactUtilsTest(final String url, final boolean expectedResult) {
        this.url = url;
        this.expectedResult = expectedResult;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return newArrayList(new Object[]{
            "group/artifact", false
        }, new Object[]{
            "artifact", false
        }, new Object[]{
            "/group/artifact/", false
        }, new Object[]{
            "group/artifact/", true
        }, new Object[]{
            "group/artifact.name/", true
        });
    }

    @Test
    public void testValidatePath() {
        assertThat(validatePath(url), is(expectedResult));
    }
}
