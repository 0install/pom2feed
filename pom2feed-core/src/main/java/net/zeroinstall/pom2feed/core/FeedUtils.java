package net.zeroinstall.pom2feed.core;

import com.google.common.base.Charsets;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Throwables;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;

/**
 * Utility class for creating Zero Install feeds.
 */
final class FeedUtils {

    /**
     * A regular expression describing dotted lists.
     */
    private static final Pattern dottedListPattern = Pattern.compile("^[0-9]+(\\.[0-9]+)*$");

    private FeedUtils() {
    }

    /**
     * Converts a Maven version string into a Zero Install version string.
     */
    public static String pom2feedVersion(String pomVersion) {
        String[] parts = checkNotNull(pomVersion).split("-");
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

        return prefix + part;
    }

    public static String convertToAsciiNumbers(String value) {
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            builder.append(String.format("%03d", (byte) c));
        }
        return builder.toString();
    }

    /**
     * Calculates a sha1 digest for a single file manifest.
     *
     * @param hash The sha1 hash of the file.
     * @param size The size of the file.
     * @param fileName The name of the file.
     * @return The hex-encoded sha1 digest.
     */
    public static String getSha1ManifestDigest(String hash, long size, String fileName) {
        String manifest = "F "
                + checkNotNull(hash)
                + " 0 " // modification timestamp
                + size + " "
                + checkNotNull(fileName) + "\n";
        return sha1(manifest);
    }

    private static String sha1(String data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw Throwables.propagate(ex);
        }

        byte[] digest;
        try {
            digest = md.digest(data.getBytes(Charsets.UTF_8.name()));
        } catch (UnsupportedEncodingException ex) {
            throw Throwables.propagate(ex);
        }
        return Hex.encodeHexString(digest);
    }
}
