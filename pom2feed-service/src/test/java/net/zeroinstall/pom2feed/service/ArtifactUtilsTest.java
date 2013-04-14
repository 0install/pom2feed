package net.zeroinstall.pom2feed.service;

import static net.zeroinstall.pom2feed.service.ArtifactUtils.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ArtifactUtilsTest {

    @Test
    public void testValidatePathNoSlashAtEnd() {
        assertFalse(validatePath("group/artifact"));
    }

    @Test
    public void testValidatePathTooShort() {
        assertFalse(validatePath("artifact"));
    }

    @Test
    public void testValidatePathInvalidCharacters() {
        assertFalse(validatePath("group.sub/artifact/"));
    }

    @Test
    public void testValidatePathSlashAtStart() {
        assertFalse(validatePath("/group/artifact/"));
    }

    @Test
    public void testValidatePathValid() {
        assertTrue(validatePath("group/artifact/"));
    }
}
