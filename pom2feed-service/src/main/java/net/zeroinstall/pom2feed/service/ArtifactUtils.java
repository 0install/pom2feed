package net.zeroinstall.pom2feed.service;

/**
 * Utility class with helper methods for Maven artifacts.
 */
public final class ArtifactUtils {

    private ArtifactUtils() {
    }

    /**
     * Checks that a string is a valid artifact URI path.
     */
    public static boolean validatePath(final String value) {
        String[] parts = value.split("/");
        if (parts.length < 2) {
            return false;
        }
        for (String part : parts) {
            if (!part.matches("[A-Za-z0-9_-]+")) {
                return false;
            }
        }
        return true;
    }
}
