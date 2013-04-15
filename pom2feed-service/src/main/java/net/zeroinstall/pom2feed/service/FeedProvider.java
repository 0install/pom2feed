package net.zeroinstall.pom2feed.service;

import java.io.IOException;
import org.apache.maven.model.building.ModelBuildingException;
import org.xml.sax.SAXException;

/**
 * Provides Zero Install feeds for specific Maven artifacts.
 */
public interface FeedProvider {

    /**
     * Provides a Zero Install feed for a specific Maven artifact.
     *
     * @param artifactPath The path used to request the artifact from a Maven
     * server (artifact group and id combined).
     * @return The serialized feed data.
     * @throws IOException Download of one the Maven source files failed.
     * @throws SAXException Parsing of one the Maven source files failed.
     * @throws ModelBuildingException Maven source model is inconsistent.
     */
    String getFeed(String artifactPath) throws IOException, SAXException, ModelBuildingException;
}
