package net.zeroinstall.pom2feed.core;

import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static net.zeroinstall.pom2feed.core.VersionUtils.pom2feedVersionRange;

@RunWith(value = Parameterized.class)
public class VersionUtilsRangeTest {

    private final String mavenRange, ziRange;

    public VersionUtilsRangeTest(String mavenVersion, String ziVersion) {
        this.mavenRange = mavenVersion;
        this.ziRange = ziVersion;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"1.0", "1.0"},
            {"1.0-snapshot", "1.0-pre"},
            {"(,1.0]", "..!1.0-post"},
            {"(,1.0)", "..!1.0"},
            {"[1.0,)", "1.0.."},
            {"(1.0,)", "1.0-post.."}, 
            {"(1.0,2.0)", "1.0-post..!2.0"},
            {"[1.0,2.0]", "1.0..!2.0-post"},
            {"[1.0,2.0)", "1.0..!2.0"},
            {"1.0,2.0", "1.0|2.0"},
            {"1.0,[2.0,)", "1.0|2.0.."},
            {"[2.0,),1.0", "2.0..|1.0"},
            {"[1.0,2.0),[3.0,4.0)", "1.0..!2.0|3.0..!4.0"},
            {"[1.0,),[2.0,)", "1.0..|2.0.."},
            {"[1.0,),(,2.0]", "1.0..|..!2.0-post"}
        });
    }

    @Test
    public void testPom2feedVersionRange() {
        assertEquals(ziRange, pom2feedVersionRange(mavenRange));
    }
}