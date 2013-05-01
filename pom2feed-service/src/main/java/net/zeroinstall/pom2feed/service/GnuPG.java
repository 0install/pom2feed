package net.zeroinstall.pom2feed.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.ByteStreams.toByteArray;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Utility class for interacting with GnuPG.
 */
final class GnuPG {

    private GnuPG() {
    }

    public static String getPublicKey(String keySpecifier) throws IOException {
        try {
            Process process = new ProcessBuilder(
                    "gpg", "-a", "--export", checkNotNull(keySpecifier)).
                    start();

            if (process.waitFor() != 0) {
                String errorMessage = new Scanner(process.getErrorStream()).useDelimiter("\\A").next();
                throw new IOException(errorMessage);
            }

            return new Scanner(process.getInputStream()).useDelimiter("\\A").next();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    public static byte[] detachSign(String data, String keySpecifier) throws IOException {
        try {
            Process process = new ProcessBuilder(
                    "gpg", "--detach-sign", "--default-key", checkNotNull(keySpecifier), "--output", "-", "-").
                    start();

            PrintWriter writer = new PrintWriter(process.getOutputStream());
            writer.write(data);
            writer.close();

            if (process.waitFor() != 0) {
                String errorMessage = new Scanner(process.getErrorStream()).useDelimiter("\\A").next();
                throw new IOException(errorMessage);
            }

            return toByteArray(process.getInputStream());
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }
}
