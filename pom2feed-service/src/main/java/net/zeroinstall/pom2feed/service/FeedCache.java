package net.zeroinstall.pom2feed.service;

/**
 * Caches requests for feeds in a thread-safe manner.
 */
public class FeedCache implements FeedProvider {

    private final FeedProvider backingProvider;

    public FeedCache(FeedProvider backingProvider) {
        this.backingProvider = backingProvider;
    }

    @Override
    public String getFeed(String[] artifactPath) {
        // TODO: Add caching logic
        return backingProvider.getFeed(artifactPath);
    }
}
