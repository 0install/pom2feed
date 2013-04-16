package net.zeroinstall.pom2feed.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import static net.zeroinstall.pom2feed.core.UrlUtils.ensureSlashEnd;
import org.apache.maven.model.Model;

/**
 * Utility class for creating interacting with Maven.
 */
public final class MavenUtils {

    /**
     * A regular expression describing valid maven IDs.
     */
    private static final Pattern mavenIdPattern = Pattern.compile("^[A-Za-z0-9_\\.-]+$");

    private MavenUtils() {
    }

    /**
     * Returns a pom2feed service URL for a specific artifact.
     *
     * @param pom2feedService The base URL of the pom2feed service.
     * @param groupId The group ID of the artifact.
     * @param artifactId The artifact ID.
     */
    public static String getServiceUrl(URL pom2feedService, String groupId, String artifactId) {
        checkNotNull(pom2feedService);
        checkArgument(mavenIdPattern.matcher(checkNotNull(groupId)).matches());
        checkArgument(mavenIdPattern.matcher(checkNotNull(artifactId)).matches());

        return ensureSlashEnd(pom2feedService).toString()
                + groupId.replace('.', '/') + '/'
                + artifactId.replace('.', '/') + '/';
    }

    /**
     * Returns the repository URL for a Maven artifact file.
     *
     * @param mavenRepository The base URL of the Maven repository.
     * @param model The Maven model describing the artifact to get.
     * @param groupId The artifact ID.
     * @param artifactId The artifact ID.
     * @param version The artifact version.
     * @param fileType The file type to return (e.g. JAR or POM).
     */
    public static URL getArtifactFileUrl(URL mavenRepository, String groupId, String artifactId, String version, String fileType) {
        checkNotNull(mavenRepository);
        checkArgument(mavenIdPattern.matcher(checkNotNull(groupId)).matches());
        checkArgument(mavenIdPattern.matcher(checkNotNull(artifactId)).matches());
        checkArgument(mavenIdPattern.matcher(checkNotNull(version)).matches());
        checkArgument(mavenIdPattern.matcher(checkNotNull(fileType)).matches());

        try {
            return new URL(ensureSlashEnd(mavenRepository).toString()
                    + groupId.replace('.', '/') + '/'
                    + artifactId.replace('.', '/') + '/'
                    + version + '/'
                    + getArtifactFileName(artifactId, version, fileType));
        } catch (MalformedURLException ex) {
            throw propagate(ex);
        }
    }

    /**
     * Returns the file name of a Maven artifact file.
     *
     * @param artifactId The artifact ID.
     * @param version The artifact version.
     * @param fileType The file type to return (e.g. JAR or POM).
     */
    public static String getArtifactFileName(String artifactId, String version, String fileType) {
        checkArgument(mavenIdPattern.matcher(checkNotNull(artifactId)).matches());
        checkArgument(mavenIdPattern.matcher(checkNotNull(version)).matches());
        checkArgument(mavenIdPattern.matcher(checkNotNull(fileType)).matches());

        return artifactId + "-" + version + "." + fileType;
    }

    /**
     * Returns the file name of a local Maven artifact file (final name).
     *
     * @param model The Maven model describing the artifact to get.
     */
    public static String getArtifactLocalFileName(Model model) {
        checkNotNull(model);
        return (model.getBuild() != null && model.getBuild().getFinalName() != null)
                // Use custom final name
                ? model.getBuild().getFinalName() + "." + model.getPackaging()
                // Use default name
                : getArtifactFileName(model.getArtifactId(), model.getVersion(), model.getPackaging());
    }
}
