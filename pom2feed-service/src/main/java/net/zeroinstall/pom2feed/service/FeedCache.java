package net.zeroinstall.pom2feed.service;

import com.google.common.base.Throwables;
import com.google.common.cache.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Caches requests for feeds in a thread-safe manner.
 */
public class FeedCache implements FeedProvider {

    private final LoadingCache<String, String> cache;

    public FeedCache(final FeedProvider backingProvider) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) {
                        return backingProvider.getFeed(key);
                    }
                });
    }

    @Override
    public String getFeed(final String artifactPath) {
        try {
            return cache.get(artifactPath);
        } catch (ExecutionException ex) {
            Throwables.propagate(ex);
            return null;
        }
    }
}
