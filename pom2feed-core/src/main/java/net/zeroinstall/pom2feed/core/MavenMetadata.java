package net.zeroinstall.pom2feed.core;

import static com.google.common.base.Throwables.propagate;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Represents versioning metadata for a Maven artifact.
 */
public class MavenMetadata {

    private final String groupId;
    private final String artifactId;
    private final String latestVersion;
    private final List<String> versions;

    /**
     * Creates a new versioning metadata instance.
     *
     * @param groupId The group ID.
     * @param artifactId The artifact ID.
     * @param latestVersion The latest version of the artifact.
     * @param versions A list of all versions of the artifact.
     */
    public MavenMetadata(String groupId, String artifactId, String latestVersion, Iterable<String> versions) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.latestVersion = latestVersion;
        this.versions = new ImmutableList.Builder<String>().addAll(versions).build();
    }

    /**
     * Loads versioning metadata from a remote XML file.
     *
     * @throws IOException Download of the source file failed.
     * @throws SAXException Parsing of the source file failed.
     */
    public static MavenMetadata load(URL url) throws IOException, SAXException {
        return load(url.openStream());
    }

    /**
     * Returns the group ID.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Returns the artifact ID.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the latest version of the artifact.
     */
    public String getLatestVersion() {
        return (latestVersion == null)
                ? versions.get(versions.size() - 1)
                : latestVersion;
    }

    /**
     * Returns a list of all versions of the artifact.
     */
    public List<String> getVersions() {
        return versions;
    }

    /**
     * Loads versioning metadata from an XML stream.
     *
     * @throws IOException Reading of the source file failed.
     * @throws SAXException Parsing of the source file failed.
     */
    public static MavenMetadata load(InputStream stream) throws IOException, SAXException {
        DocumentBuilder docBuilder;
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw propagate(ex);
        }

        Document doc = docBuilder.parse(stream);
        Element versioning = (Element) doc.getElementsByTagName("versioning").item(0);
        return new MavenMetadata(getGroupId(doc), getArtifactId(doc), getLatestVersion(versioning), getVersions(versioning));
    }

    private static String getGroupId(Document doc) throws DOMException {
        String groupId = doc.getElementsByTagName("groupId").item(0).getTextContent();
        return groupId;
    }

    private static String getArtifactId(Document doc) throws DOMException {
        String artifactId = doc.getElementsByTagName("artifactId").item(0).getTextContent();
        return artifactId;
    }

    private static List<String> getVersions(Element versioning) throws DOMException {
        Element versionsContainer = (Element) versioning.getElementsByTagName("versions").item(0);
        NodeList versionTags = versionsContainer.getElementsByTagName("version");
        List<String> versions = new LinkedList<String>();
        for (int i = 0; i < versionTags.getLength(); i++) {
            versions.add(versionTags.item(i).getTextContent());
        }
        return versions;
    }

    private static String getLatestVersion(Element versioning) throws DOMException {
        Node latestVersionNode = versioning.getElementsByTagName("latest").item(0);
        String latestVersion = (latestVersionNode == null) ? null : latestVersionNode.getTextContent();
        return latestVersion;
    }
}
