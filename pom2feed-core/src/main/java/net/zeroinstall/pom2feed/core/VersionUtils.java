package net.zeroinstall.pom2feed.core;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for converting Maven versions to Zero Install versions.
 */
final class VersionUtils {

    /**
     * A regular expression describing dotted lists.
     */
    private static final Pattern dottedListPattern = Pattern.compile("^[0-9]+(\\.[0-9]+)*$");

    private VersionUtils() {
    }

    /**
     * Converts a Maven version string into a Zero Install version string.
     */
    public static String pom2feedVersion(String mavenVersion) {
        String[] parts = checkNotNull(mavenVersion.trim()).split("-");
        StringBuilder ziVersion = new StringBuilder();

        if (!isDottedList(parts[0])) {
            ziVersion.append("1-");
        }

        for (int i = 0; i < parts.length; i++) {
            ziVersion.append(pom2feedVersionPart(parts[i]));

            if (i < parts.length - 1) {
                ziVersion.append("-");
            }
        }

        return ziVersion.toString();
    }

    private static boolean isDottedList(String part) {
        return dottedListPattern.matcher(part).matches();
    }
    /**
     * Maps Maven version qualifiers to Zero Install version qualifiers.
     */
    private static final String[][] QUALIFIER_MAP = new String[][]{
        {"snapshot", "pre-"},
        {"alpha", "pre1-"},
        {"a", "pre1-"},
        {"beta", "pre2-"},
        {"b", "pre2-"},
        {"milestone", "pre3-"},
        {"m", "pre3-"},
        {"rc", "rc"},
        {"cr", "rc"},
        {"ga", "0"},
        {"final", "0"},
        {"rev", "post-"},
        {"sp", "post"}
    };

    private static String pom2feedVersionPart(String part) {
        String prefix = "";
        for (String[] pair : QUALIFIER_MAP) {
            if (part.toLowerCase(Locale.ENGLISH).startsWith(pair[0])) {
                part = part.substring(pair[0].length());
                prefix = pair[1];
                break;
            }
        }

        if (!isDottedList(part)) {
            part = convertToAsciiNumbers(part);
        }

        if (prefix.endsWith("-") && part.equals("")) {
            return prefix.substring(0, prefix.length() - 1);
        } else {
            return prefix + part;
        }
    }

    static String convertToAsciiNumbers(String value) {
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            builder.append(String.format("%03d", (byte) c));
        }
        return builder.toString();
    }
}
