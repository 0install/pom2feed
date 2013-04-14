package net.zeroinstall.pom2feed.core;

import java.net.URI;
import org.junit.*;
import static org.junit.Assert.*;
import static net.zeroinstall.pom2feed.core.MavenUtils.*;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;

public class MavenUtilsTest {

    @Test
    public void testGetServiceUri() {
        assertEquals(
                "http://0install.de/maven/group/subgroup/artifact/subartifact/",
                getServiceUri(URI.create("http://0install.de/maven"), "group.subgroup", "artifact.subartifact"));
        assertEquals(
                "http://0install.de/maven/group/subgroup/artifact/subartifact/",
                getServiceUri(URI.create("http://0install.de/maven/"), "group.subgroup", "artifact.subartifact"));
    }

    @Test
    public void testGetArtifactFileName() {
        assertEquals("artifact.subartifact-1.0.jar", getArtifactFileName("artifact.subartifact", "1.0", "jar"));
    }

    @Test
    public void testGetArtifactLocalFileName() {
        Model model = new Model();
        model.setGroupId("group.subgroup");
        model.setArtifactId("artifact.subartifact");
        model.setVersion("1.0");
        model.setPackaging("jar");

        assertEquals("artifact.subartifact-1.0.jar", getArtifactLocalFileName(model));
    }

    @Test
    public void testGetArtifactLocalFileNameOverride() {
        Model model = new Model();
        Build build = new Build();
        build.setFinalName("test");
        model.setBuild(build);
        model.setGroupId("group.subgroup");
        model.setArtifactId("artifact.subartifact");
        model.setVersion("1.0");
        model.setPackaging("jar");

        assertEquals("test.jar", getArtifactLocalFileName(model));
    }

    @Test
    public void testGetArtifactFileUri() {
        assertEquals(
                "http://0install.de/maven/group/subgroup/artifact/subartifact/1.0/artifact.subartifact-1.0.pom",
                getArtifactFileUri(URI.create("http://0install.de/maven"), "group.subgroup", "artifact.subartifact", "1.0", "pom"));
        assertEquals(
                "http://0install.de/maven/group/subgroup/artifact/subartifact/1.0/artifact.subartifact-1.0.pom",
                getArtifactFileUri(URI.create("http://0install.de/maven/"), "group.subgroup", "artifact.subartifact", "1.0", "pom"));
    }
}
