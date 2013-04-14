package net.zeroinstall.pom2feed.service;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class with helper methods for Maven artifacts.
 */
public final class ArtifactUtils {

    private ArtifactUtils() {
    }

    /**
     * Checks that a string is a valid artifact URI path.
     */
    public static boolean validatePath(String value) {
        checkNotNull(value);

        // Must end with slash
        if (!value.endsWith("/")) {
            return false;
        }
        value = value.substring(0, value.length() - 1);

        String[] parts = value.split("/");

        // Must contain at least two blocks (group and artifact ID)
        if (parts.length < 2) {
            return false;
        }

        // Must only contain characters valid for Maven IDs minus the dot (.)
        for (String part : parts) {
            if (!part.matches("[A-Za-z0-9_-]+")) {
                return false;
            }
        }
        return true;
    }
}
