package com.axel.test.service;

import net.sf.ehcache.config.CacheConfiguration;

import java.io.Serializable;
import java.util.List;

public interface CachingService<K extends Serializable, V extends Serializable> {

    String ARTICLE_CACHE = "article";
    String AUTHOR_CACHE = "author";
    String KEYWORD_CACHE = "keyword";
    String DATE_CACHE = "date";

    void createCache(CacheConfiguration configuration);

    /**
     * Does cache exist.
     *
     * @param cacheName
     *        the cache name
     * @return true, if successful
     */
    boolean doesCacheExist(String cacheName);

    /**
     * Gets the cache keys.
     *
     * @param cacheName
     *        the cache name
     * @return the keys
     */
    List<K> getKeys(String cacheName);

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
    V getObject(String cacheName, K key);

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
    void putObject(String cacheName, K key, V value);

    /**
     * Removes the object from the cache.
     *
     * @param cacheName
     *        the cache name
     * @param key
     *        the key
     */
    void removeObject(String cacheName, K key);
}
