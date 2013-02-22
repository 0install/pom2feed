package net.zeroinstall.pom2feed.core;

import java.net.URI;
import net.zeroinstall.model.*;
import org.apache.maven.model.*;
import org.junit.*;
import static org.junit.Assert.*;

public class FeedBuilderTest {

    private FeedBuilder builder = new FeedBuilder(URI.create("http://zeroinstall.net/service/test/"));

    @Test
    public void testAddMetadata() {
        Model model = new Model();

        InterfaceDocument document = builder.addMetadata(model).getDocument();
    }

    @Test
    public void testAddLocalImplementation() {
        Build build = new Build();
        build.setFinalName("group-artifact-1.0");

        Model model = new Model();
        model.setBuild(build);

        InterfaceDocument document = builder.addLocalImplementation(model).getDocument();
    }

    @Test
    public void testAddRemoteImplementation() {
        Build build = new Build();
        build.setFinalName("group-artifact-1.0");

        Model model = new Model();
        model.setBuild(build);

        InterfaceDocument document = builder.addRemoteImplementation(model, URI.create("http://zeroinstall.net/files/test.jar")).getDocument();
    }
}
