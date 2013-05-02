package net.zeroinstall.pom2feed.core;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.hash.Hashing;

/**
 * Utility class for creating Zero Install manifest digests.
 */
final class ManifestUtils {

    private ManifestUtils() {
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
        return Hashing.sha1().hashString(manifest, UTF_8).toString();
    }
}
