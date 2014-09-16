package net.zeroinstall.pom2feed.core;

import com.google.common.base.Joiner;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Throwables.propagate;
import com.google.common.collect.ImmutableList;
import java.io.*;
import java.net.URL;
import static java.net.URLEncoder.encode;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Represents versioning metadata for a Maven artifact.
 */
public class MavenMetadata {

    private static final XPathExpression groupIdPath, artifactIdPath, versionsPath, latestVersionPath, versionsQueryPath;

    static {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            groupIdPath = xpath.compile("//groupId/text()");
            artifactIdPath = xpath.compile("//artifactId/text()");
            versionsPath = xpath.compile("//versioning//versions//version/text()");
            latestVersionPath = xpath.compile("//versioning//latest/text()");
            versionsQueryPath = xpath.compile("//result[@name='response']//doc/str[@name='v']/text()");
        } catch (XPathExpressionException ex) {
            throw propagate(ex);
        }
    }
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
    public static MavenMetadata load(URL url) throws IOException, SAXException, XPathExpressionException {
        return parse(url.openStream());
    }

    /**
     * Parses versioning metadata from an XML stream.
     *
     * @throws IOException Reading of the metadata file failed.
     * @throws SAXException Parsing of the metadata file failed.
     */
    public static MavenMetadata parse(InputStream stream) throws IOException, SAXException, XPathExpressionException {
        Document doc = getDocumentBuilder().parse(stream);

        String groupId = getValue(doc, groupIdPath);
        String artifactId = getValue(doc, artifactIdPath);
        List<String> versions = getValueSet(doc, versionsPath);
        String latestVersion = getValue(doc, latestVersionPath);
        if (isNullOrEmpty(latestVersion)) {
            latestVersion = versions.get(versions.size() - 1);
        }

        return new MavenMetadata(groupId, artifactId, latestVersion, versions);
    }

    /**
     * Retrieves versioning metadata from a search query.
     *
     * @throws IOException Download of the query data failed.
     * @throws SAXException Parsing of the query data failed.
     */
    public static MavenMetadata query(URL queryService, String artifactPath) throws IOException, SAXException, XPathExpressionException {
        String[] parts = artifactPath.split("/");
        String groupId = Joiner.on(".").join(Arrays.copyOfRange(parts, 0, parts.length - 1));
        String artifactId = parts[parts.length - 1];

        URL url = new URL(queryService, "select?q="
                + "g:%22" + encode(groupId, "UTF-8") + "%22+AND+"
                + "a:%22" + encode(artifactId, "UTF-8") + "%22"
                + "&core=gav&wt=xml");
        InputStream stream = url.openStream();
        Document doc = getDocumentBuilder().parse(stream);

        List<String> versions = getValueSet(doc, versionsQueryPath);
        if (versions.isEmpty()) {
            throw new IOException("Unknown artifact ID");
        }
        String latestVersion = versions.get(0);

        return new MavenMetadata(groupId, artifactId, latestVersion, versions);
    }

    private static String getValue(Document doc, XPathExpression expression) throws XPathExpressionException, DOMException {
        NodeList nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        Node item = nodes.item(0);
        return (item == null) ? null : item.getNodeValue();
    }

    private static List<String> getValueSet(Document doc, XPathExpression expression) throws XPathExpressionException, DOMException {
        NodeList nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        List<String> values = new LinkedList<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            values.add(nodes.item(i).getNodeValue());
        }
        return values;
    }

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw propagate(ex);
        }
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
        return latestVersion;
    }

    /**
     * Returns a list of all versions of the artifact.
     */
    public List<String> getVersions() {
        return versions;
    }
}
