package net.zeroinstall.pom2feed.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * Utility class for handling URLs.
 */
public final class UrlUtils {

    private UrlUtils() {
    }

    public static URL ensureSlashEnd(URL url) {
        checkNotNull(url);
        try {
            return (url.toString().endsWith("/")) ? url : new URL(url.toString() + "/");
        } catch (MalformedURLException ex) {
            throw propagate(ex);
        }
    }

    public static long getRemoteFileSize(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        return connection.getContentLength();
    }

    public static String getRemoteWord(URL url) throws IOException {
        Scanner scanner = new Scanner(url.openStream());
        try {
            return scanner.next();
        } finally {
            scanner.close();
        }
    }
}
