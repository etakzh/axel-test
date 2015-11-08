package com.axel.test.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CachingServiceImpl<K extends Serializable, V extends Serializable> implements CachingService<K, V>{

    private Logger LOG = LoggerFactory.getLogger(CachingServiceImpl.class);

    private final CacheManager _cacheManager = CacheManager.create();

    private final Map<String, Object> _syncLockMap = new ConcurrentHashMap<String, Object>();

    @PreDestroy
    private void shutdown()
    {
        if (_cacheManager != null)
        {
            _cacheManager.shutdown();
        }
    }

    @PostConstruct
    private void initializeCachingManager()
    {
        // Remove all cached data that could be stored on a disk after previous application or test run.
        if (_cacheManager != null)
        {
            _cacheManager.removeAllCaches();
        }
        this.createCache(getCacheConfiguration(ARTICLE_CACHE));
        this.createCache(getCacheConfiguration(AUTHOR_CACHE));
        this.createCache(getCacheConfiguration(DATE_CACHE));
        this.createCache(getCacheConfiguration(KEYWORD_CACHE));
    }

    private CacheConfiguration getCacheConfiguration(final String name) {
        final CacheConfiguration configuration = new CacheConfiguration();
        configuration.name(name);
        configuration.maxEntriesLocalHeap(1000);
        configuration.memoryStoreEvictionPolicy("LFU");
        configuration.timeToLiveSeconds(0);
        configuration.timeToIdleSeconds(0);

        // Currently the disk persistent feature is not required and hence not enabled
        configuration.eternal(false);
        configuration.persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE));
        return configuration;
    }

    /**
     * Creates the cache with the specified configuration. If the cache already
     * exists with the same name it simply ignores the creation
     *
     * @param configuration
     *        the cache configuration info
     */
    @Override
    public void createCache(CacheConfiguration configuration)
    {
        Validate.notNull(configuration, "Invalid CacheConfigurationInfo specified.");

        LOG.info("Creating cache {0}.", configuration.getName());
        if (doesCacheExist(configuration.getName()))
        {
            LOG.debug("Cache already exists: " + configuration.getName());
            return;
        }

        Cache cache = new Cache(configuration);
        _cacheManager.addCache(cache);
        // Create a sync key to synchronize multiple thread calls
        _syncLockMap.put(configuration.getName(), new Object());
        LOG.debug("Created the Cache: " + configuration.getName());
    }

    /**
     * Puts the object in the cache.
     *
     * @param cacheName
     *        the cache name
     * @param key
     *        the key
     * @param value
     *        the value
     */
    @Override
    public void putObject(String cacheName, K key, V value)
    {
        synchronized (_syncLockMap.get(cacheName))
        {
            getCache(cacheName).put(new Element(key, value));
        }
    }

    private V getCacheValue(K key, Cache cache)
    {
        Element element = cache.get(key);
        if (element != null)
        {
            LOG.debug("Getting Object Value from the Cache: "
                    + cache.getName());
            return (V) element.getObjectValue();
        }
        return null;
    }

    /**
     * Does cache exist.
     *
     * @param cacheName
     *        the cache name
     * @return true, if successful
     */
    @Override
    public boolean doesCacheExist(String cacheName)
    {
        return _cacheManager.cacheExists(cacheName);
    }

    /**
     * Gets the cache keys.
     *
     * @param cacheName
     *        the cache name
     * @return the keys
     */
    @Override
    public List<K> getKeys(String cacheName)
    {
        Cache cache = getCache(cacheName);
        if (cache != null)
        {
            return new LinkedList<K>(cache.getKeys());
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Gets the cache from the CacheManager managed by Ehcache .
     *
     * @param cacheName
     *        the cache name
     * @return the cache
     */
    private Cache getCache(String cacheName)
    {
        return _cacheManager.getCache(cacheName);
    }

    /**
     * Gets the object only from the specified cache.
     *
     * @param cacheName
     *        the cache name
     * @param key
     *        the key
     * @return the cache value for the specified key. If the cache doesn't
     *         exists then a NULL value is returned.
     */
    @Override
    public V getObject(String cacheName, K key)
    {
        LOG.debug("Inside GetObject method ");
        Validate.notNull(cacheName, "Cache Name cannot be null.");
        Validate.notNull(key, "Cache key cannot be null.");

        Cache cache = getCache(cacheName);
        V value = null;
        synchronized (_syncLockMap.get(cacheName))
        {
            LOG.debug("Start Executing Sync Block");
            if (cache != null)
            {
                value = getCacheValue(key, cache);
            }
        }

        return value;
    }

    /**
     * Removes the object from the cache.
     *
     * @param cacheName
     *        the cache name
     * @param key
     *        the key
     */
    @Override
    public void removeObject(String cacheName, K key)
    {
        synchronized (_syncLockMap.get(cacheName))
        {
            getCache(cacheName).remove(key);
        }
    }
}
