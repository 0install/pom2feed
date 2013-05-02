package net.zeroinstall.pom2feed.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
        long size = connection.getContentLength();
        return size;
    }

    public static String getRemoteLine(URL url) throws IOException {
        InputStream stream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String hash = reader.readLine();
        return hash;
    }
}
