package net.zeroinstall.pom2feed.service;

import static com.google.common.base.Throwables.*;
import com.google.common.cache.*;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.IOException;
import java.util.concurrent.*;
import org.apache.maven.model.building.ModelBuildingException;
import org.xml.sax.SAXException;

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
                            public String load(String key) throws Exception {
                                return backingProvider.getFeed(key);
                            }
                        });
    }

    @Override
    public String getFeed(final String artifactPath) throws IOException, SAXException, ModelBuildingException {
        try {
            return cache.get(artifactPath);
        } catch (ExecutionException ex) {
            propagateIfInstanceOf(ex.getCause(), IOException.class);
            propagateIfInstanceOf(ex.getCause(), SAXException.class);
            propagateIfInstanceOf(ex.getCause(), ModelBuildingException.class);
            throw propagate(ex);
        } catch (UncheckedExecutionException ex) {
            propagateIfInstanceOf(ex.getCause(), IOException.class);
            propagateIfInstanceOf(ex.getCause(), SAXException.class);
            propagateIfInstanceOf(ex.getCause(), ModelBuildingException.class);
            throw ex;
        }
    }
}
