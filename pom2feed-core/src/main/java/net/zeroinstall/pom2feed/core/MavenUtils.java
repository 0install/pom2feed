package net.zeroinstall.pom2feed.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.net.URI;
import org.apache.maven.model.Model;

/**
 * Utility class for creating interacting with Maven.
 */
final class MavenUtils {

    private MavenUtils() {
    }

    /**
     * Returns a pom2feed service URI for a specific artifact.
     *
     * @param pom2feedService The base URI of the pom2feed service.
     * @param groupId The group ID of the artifact.
     * @param artifactId The artifact ID.
     */
    public static String getServiceUri(URI pom2feedService, String groupId, String artifactId) {
        checkNotNull(pom2feedService);
        checkNotNull(groupId);
        checkNotNull(artifactId);
        checkArgument(groupId.matches("[A-Za-z0-9_\\.-]+"));
        checkArgument(artifactId.matches("[A-Za-z0-9_\\.-]+"));

        String serviceString = pom2feedService.toString();
        if (!serviceString.endsWith("/")) {
            serviceString = serviceString + "/";
        }

        return serviceString
                + groupId.replace('.', '/') + '/'
                + artifactId.replace('.', '/') + '/';
    }

    /**
     * Returns the download URI for a Maven artifact file.
     *
     * @param mavenRepository The base URI of the Maven repository.
     * @param model The Maven model describing the artifact to get.
     */
    public static URI getArtifactFileUri(URI mavenRepository, Model model) {
        checkNotNull(mavenRepository);
        checkNotNull(model);
        checkArgument(model.getGroupId().matches("[A-Za-z0-9_\\.-]+"));
        checkArgument(model.getArtifactId().matches("[A-Za-z0-9_\\.-]+"));
        checkArgument(model.getVersion().matches("[A-Za-z0-9_\\.-]+"));

        String repositoryString = mavenRepository.toString();
        if (!repositoryString.endsWith("/")) {
            repositoryString = repositoryString + "/";
        }

        return URI.create(repositoryString
                + model.getGroupId().replace('.', '/') + '/'
                + model.getArtifactId().replace('.', '/') + '/'
                + model.getVersion() + '/'
                + getArtifactFileName(model));
    }

    /**
     * Returns the file name of a Maven artifact file.
     *
     * @param model The Maven model describing the artifact to get.
     */
    public static String getArtifactFileName(Model model) {
        checkNotNull(model);
        return model.getArtifactId() + "-" + model.getVersion() + "." + model.getPackaging();
    }

    /**
     * Returns the file name of a local Maven artifact file (final name).
     *
     * @param model The Maven model describing the artifact to get.
     */
    public static String getArtifactLocalFileName(Model model) {
        checkNotNull(model);
        if (model.getBuild() != null || model.getBuild().getFinalName() != null) {
            return model.getBuild().getFinalName() + "." + model.getPackaging();
        } else {
            return getArtifactFileName(model);
        }
    }
}
