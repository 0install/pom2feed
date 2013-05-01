package net.zeroinstall.pom2feed.core;

import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static net.zeroinstall.pom2feed.core.VersionUtils.*;

@RunWith(value = Parameterized.class)
public class VersionUtilsTest {

    private final String mavenVersion, ziVersion;

    public VersionUtilsTest(String mavenVersion, String ziVersion) {
        this.mavenVersion = mavenVersion;
        this.ziVersion = ziVersion;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"1.0-2", "1.0-2"},
            {" 1.0-2 ", "1.0-2"},
            {"1.0-snapshot", "1.0-pre"},
            {"1.0-snapshot123", "1.0-pre-123"},
            {"1.0-milestone123", "1.0-pre1-123"},
            {"1.0-milestone1b", "1.0-pre1-049098"},
            {"1.0-alpha123", "1.0-pre2-123"},
            {"1.0-a123", "1.0-pre2-123"},
            {"1.0-beta123", "1.0-pre3-123"},
            {"1.0-b123", "1.0-pre3-123"},
            {"1.0-m123", "1.0-pre1-123"},
            {"1.0-m1b", "1.0-pre1-049098"},
            {"1.0-rc123", "1.0-rc123"},
            {"1.0-cr123", "1.0-rc123"},
            {"1.0-ga", "1.0-0"},
            {"1.0-final", "1.0-0"},
            {"1.0-rev123", "1.0-post-123"},
            {"1.0-sp123", "1.0-post123"},
            {"1.0-xyz", "1.0-120121122"},
            {"rc123", "1-rc123"},
            {"xyz", "1-120121122"}
        });
    }

    @Test
    public void testConvertVersion() {
        assertEquals(ziVersion, convertVersion(mavenVersion));
    }

    @Test
    public void testConvertToAsciiNumbers() {
        assertEquals("097098099", convertToAsciiNumbers("abc"));
    }
}