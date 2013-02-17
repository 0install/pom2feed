package net.zeroinstall.pom2feed.core;

import com.google.common.base.Throwables;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 * Utiltity classes for creating Zero Install feeds.
 */
final class FeedUtils {

    private FeedUtils() {
    }

    /**
     * Converts a Maven version string into a Zero Install version string.
     */
    public static String pom2feedVersion(String pomVersion) {
        // TODO: Handle -snapshot, rc, etc.
        return pomVersion;
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
        String manifest = "F " + hash + " 0 " + size + " " + fileName + "\n";
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
            digest = md.digest(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw Throwables.propagate(ex);
        }
        return Hex.encodeHexString(digest);
    }
}
