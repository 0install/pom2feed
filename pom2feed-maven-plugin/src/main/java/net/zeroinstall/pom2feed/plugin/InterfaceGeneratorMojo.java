package net.zeroinstall.pom2feed.plugin;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import net.zeroinstall.model.InterfaceDocument;
import net.zeroinstall.pom2feed.core.FeedBuilder;
import static net.zeroinstall.publish.FeedUtils.getFeedString;
import net.zeroinstall.publish.GnuPG;
import net.zeroinstall.publish.OpenPgp;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates a basic {@link InterfaceDocument Zero Install Interface} using the
 * projects Maven model instance (the pom.xml) and saves it to a user-defined
 * place.
 */
@Mojo(name = "generate")
public class InterfaceGeneratorMojo extends AbstractMojo {

    //<editor-fold defaultstate="collapsed" desc="statics">
    private final static String DOT_XML = ".xml";
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="plugin parameters">
    @Parameter(defaultValue = "${project.build.directory}", property = "feedDirectory", required = true)
    private File feedDirectory;
    @Parameter(defaultValue = "${project.artifactId}", property = "feedName", required = true)
    private String feedName;
    @Parameter(defaultValue = "http://repo.maven.apache.org/maven2/", property = "mavenRepository", required = true)
    private URL mavenRepository;
    @Parameter(defaultValue = "http://maven.0install.net/", property = "pom2feedService", required = true)
    private URL pom2feedService;
    @Parameter(property = "keySpecifier", required = false)
    private String keySpecifier;
    //</editor-fold>

    /**
     * An encryption/signature system compatible with the OpenPGP standard.
     */
    private final OpenPgp openPgp = new GnuPG();

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
        if (!feedDirectory.exists()) {
            if (!feedDirectory.mkdirs()) {
                throw new MojoFailureException(String.format("\"The directory %s could not be created.\"", feedDirectory));
            }
        }
        if (!feedDirectory.isDirectory()) {
            throw new MojoFailureException(String.format("\"%s is not a directory.\"", feedDirectory));
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
        final FeedBuilder feedBuilder = new FeedBuilder(mavenRepository, pom2feedService);
        feedBuilder.addMetadata(thisProject.getModel());
        feedBuilder.addLocalImplementation(thisProject.getModel(), ".");
        return feedBuilder.getDocument();
    }

    /**
     * Saves an Interface to {@link #feedDirectory} with the file name
     * {@link #feedName}.xml.
     *
     * @param interfaceToSave The Interface to save.
     * @throws MojoExecutionException Thrown, if an {@link IOException} occurs
     * while saving {@code interfaceToSave}.
     */
    private void saveInterfaceDocument(final InterfaceDocument interfaceToSave) throws MojoExecutionException {
        final File file = new File(feedDirectory, feedName + DOT_XML);
        try {
            final String feedString = getFeedString(openPgp, interfaceToSave, keySpecifier);
            Files.write(feedString, file, Charsets.UTF_8);
        } catch (final IOException exception) {
            throw new MojoExecutionException(String.format("Could not write Zero Install feed to %s", file.toString()), exception);
        }
    }
}
