package net.zeroinstall.pom2feed.service;

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
     */
    String getFeed(String artifactPath);
}
