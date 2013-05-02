package net.zeroinstall.pom2feed.service;

import java.io.IOException;
import static net.zeroinstall.pom2feed.service.GnuPG.*;
import org.junit.*;
import static org.junit.Assert.*;

@Ignore("Communicates with a real GnuPG instance, which must be configured.")
public class GnuPGTest {

    @Test
    public void testGetPublicKey() throws IOException {
        assertTrue(
                getPublicKey("E18CA35213A9C8F4391BD7C519BEF9B23A8C0090").
                startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----"));
    }

    @Test
    public void testDetachSign() throws IOException {
        byte[] signature = detachSign("data", "E18CA35213A9C8F4391BD7C519BEF9B23A8C0090");
        assertTrue(signature.length > 0);
    }
}
