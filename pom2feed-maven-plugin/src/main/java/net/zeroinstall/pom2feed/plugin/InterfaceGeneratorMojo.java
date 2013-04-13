package net.zeroinstall.pom2feed.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import net.zeroinstall.model.InterfaceDocument;
import net.zeroinstall.pom2feed.core.FeedBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.xmlbeans.XmlOptionsBean;

/**
 * Generates a basic {@link InterfaceDocument Zero Install Interface} without
 * implementations using the projects Maven model instance (means the pom.xml)
 * and saves it to a user-defined place.
 */
@Mojo(name = "generate")
public class InterfaceGeneratorMojo extends AbstractMojo {

    //<editor-fold defaultstate="collapsed" desc="statics">
    private final static String DOT_XML = ".xml";
    private final static XmlOptionsBean XML_WRITE_OPTIONS = new XmlOptionsBean();

    static {
        XML_WRITE_OPTIONS.setUseDefaultNamespace();
        XML_WRITE_OPTIONS.setSavePrettyPrint();
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="plugin parameters">
    @Parameter(defaultValue = "${project.basedir}", property = "outputDir", required = true)
    private File outputDirectory;
    @Parameter(defaultValue = "${project.artifactId}", property = "feedName", required = true)
    private String feedName;
    //</editor-fold>

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureOutputDirectoryExists();
        final InterfaceDocument generatedInterface = generateZeroInstallInterfaceFromMavenModel();
        saveInterfaceDocument(generatedInterface);
    }

    /**
     * Ensures that the output directory exists and creates it if it's absent.
     *
     * @throws MojoFailureException If the output directory path exists, but
     * it's not a directory.
     */
    private void ensureOutputDirectoryExists() throws MojoFailureException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        if (!outputDirectory.isDirectory()) {
            throw new MojoFailureException(String.format("\"%s is not a directory.\"", outputDirectory));
        }
    }

    /**
     * Retrieves the Maven Model from the plugin context and generates a Zero
     * Install Interface from it.
     *
     * @return The generated Zero Install Interface.
     */
    private InterfaceDocument generateZeroInstallInterfaceFromMavenModel() {
        final MavenProject thisProject = ((MavenProject) getPluginContext().get("project"));
        final FeedBuilder feedBuilder = new FeedBuilder(
                // TODO: Make configurable
                URI.create("http://repo.maven.apache.org/maven2/"),
                URI.create("http://0install.de/maven/"));
        feedBuilder.addMetadata(thisProject.getOriginalModel());
        return feedBuilder.getDocument();
    }

    /**
     * Saves an Interface to {@link #outputDirectory} with the file name
     * {@link #feedName}.xml.
     *
     * @param interfaceToSave The Interface to save.
     * @throws MojoExecutionException Thrown, if an {@link IOException} occurs
     * while saving {@code interfaceToSave}.
     */
    private void saveInterfaceDocument(final InterfaceDocument interfaceToSave) throws MojoExecutionException {
        final File file = new File(outputDirectory, feedName + DOT_XML);
        try {
            interfaceToSave.save(file, XML_WRITE_OPTIONS);
        } catch (final IOException exception) {
            throw new MojoExecutionException(String.format("Couldn't write Zero Install feed to %s", file.toString()), exception);
        }
    }
}
