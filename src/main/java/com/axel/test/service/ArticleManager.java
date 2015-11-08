package com.axel.test.service;

import com.axel.test.entity.Article;
import com.axel.test.exception.ArticleControllerException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.axel.test.service.CachingService.*;

/**
 * Article Manager is responsible for managing article allover the caches.
 */
@Component
public class ArticleManager {

    @Autowired
    private CachingService cacheService;

    /**
     * Add article to cache
     * @param article
     *        {@link Article}
     */
    public void addArticle(final Article article) {
        article.setId(UUID.randomUUID().toString());
        // put article in article cache
        cacheService.putObject(ARTICLE_CACHE, article.getId(), article);
        // put article id in author caches
        putUUIDToCache(AUTHOR_CACHE, article.getId(), article.getAuthorList());
        // put article id in keyword caches
        putUUIDToCache(KEYWORD_CACHE, article.getId(), article.getKeywordList());
        // put article id in date cache
        putUUIDToCache(DATE_CACHE, article.getId(), article.getPublishDate());
    }

    /**
     * Update article data in all caches.
     * @param article
     *        updated article data
     */
    public void updateArticle(final Article article) {
        final Object oldArticleRaw = cacheService.getObject(ARTICLE_CACHE, article.getId());
        if (oldArticleRaw == null) {
            throw new ArticleControllerException("Article doesn't exist by id " + article.getId());
        }
        final Article oldArticle = (Article) oldArticleRaw;
        // update author caches if needed
        if (oldArticle.getAuthorList().size() != article.getAuthorList().size() || !getJoin(oldArticle.getAuthorList()).equals(getJoin(article.getAuthorList()))) {
            removeUUIDFromCache(AUTHOR_CACHE, article.getId(), oldArticle.getAuthorList());
            putUUIDToCache(AUTHOR_CACHE, article.getId(), article.getAuthorList());
        }
        // update keyword caches if needed
        if (oldArticle.getKeywordList().size() != article.getKeywordList().size() || !getJoin(oldArticle.getKeywordList()).equals(getJoin(article.getKeywordList()))) {
            removeUUIDFromCache(KEYWORD_CACHE, article.getId(), oldArticle.getKeywordList());
            putUUIDToCache(KEYWORD_CACHE, article.getId(), article.getKeywordList());
        }
        // update date cache if needed
        if (!oldArticle.getPublishDate().equals(article.getPublishDate())) {
            removeUUIDFromCache(DATE_CACHE, article.getId(), oldArticle.getPublishDate());
            putUUIDToCache(DATE_CACHE, article.getId(), article.getPublishDate());
        }
        // update article cache
        cacheService.putObject(ARTICLE_CACHE, article.getId(), article);
    }

    /**
     * Delete article by id
     * @param id
     *        article id
     */
    public void deleteArticle(final String id) {
        final Object article = cacheService.getObject(ARTICLE_CACHE, id);
        if (article != null) {
            final Article art = (Article) article;
            // remove article id from keyword caches
            removeUUIDFromCache(KEYWORD_CACHE, id, art.getKeywordList());
            // remove article id from author caches
            removeUUIDFromCache(AUTHOR_CACHE, id, art.getAuthorList());
            // remove article id from date cache
            removeUUIDFromCache(DATE_CACHE, id, art.getPublishDate());
            // remove article from article cache
            cacheService.removeObject(ARTICLE_CACHE, art.getId());
        }
    }

    /**
     * Get article by id
     * @param id
     *        article id
     * @return {@link Article}
     */
    public Article getArticleById(final String id) {
        final Object articleRaw = cacheService.getObject(ARTICLE_CACHE, id);
        if (articleRaw != null) {
            return (Article) articleRaw;
        }
        return null;
    }

    /**
     * Get ids by author.
     * @param author
     *        author name
     * @return set of ids
     */
    public Set<String> getIdsByAuthor(final String author) {
        final Object articles = cacheService.getObject(AUTHOR_CACHE, author);
        if (articles != null) {
            return (Set<String>)articles;
        }
        return new HashSet<String>();
    }

    /**
     * Get ids of articles by period
     * @param from
     *        start of the period
     * @param to
     *        end of the period
     * @return set of ids
     */
    public Set<String> getIdsByPeriod(final LocalDate from, final LocalDate to) {
        final List keys = cacheService.getKeys(DATE_CACHE);
        final Set<String> ids = new HashSet<>();
        if (!CollectionUtils.isEmpty(keys)) {
            for (LocalDate date: (List<LocalDate>)keys) {
                if (isWithinRange(from, to, date)) {
                    ids.addAll((Set<String>)cacheService.getObject(DATE_CACHE, date));
                }
            }
        }
        return ids;
    }

    private boolean isWithinRange(final LocalDate from, final LocalDate to, LocalDate date) {
        return !date.isBefore(from) &&
                !date.isAfter(to);
    }

    /**
     * Get ids by keyword.
     * @param keyword
     *        keyword
     * @return set of ids
     */
    public Set<String> getIdsByKeyword(final String keyword) {
        final Object articles = cacheService.getObject(KEYWORD_CACHE, keyword);
        if (articles != null) {
            return (Set<String>)articles;
        }
        return new HashSet<>();
    }

    private void putUUIDToCache(final String cacheName, final String uuid, final List<String> keys) {
        for (String key : keys) {
            putUUIDToCache(cacheName, uuid, key);
        }
    }

    /**
     * Put article id to cache
     * @param cacheName
     *        actual cache name
     * @param uuid
     *        article id
     * @param key
     *        cache key
     */
    private void putUUIDToCache(String cacheName, String uuid, Object key) {
        Object set = cacheService.getObject(cacheName, (Serializable)key);
        if (set == null) {
            // using concurrent set in order to be thread-safe in runtime
            set = new CopyOnWriteArraySet<String>();
            cacheService.putObject(cacheName, (Serializable)key, (CopyOnWriteArraySet<String>) set);
        }
        ((Set) set).add(uuid);
    }

    private void removeUUIDFromCache(final String cacheName, final String uuid, final List<String> keys) {
        for (String key : keys) {
            removeUUIDFromCache(cacheName, uuid, key);
        }
    }

    /**
     * Remove article id from the cache
     * @param cacheName
     *        cache name
     * @param uuid
     *        article id
     * @param key
     *        cache key
     */
    private void removeUUIDFromCache(String cacheName, String uuid, Object key) {
        Object set = cacheService.getObject(cacheName, (Serializable)key);
        if (set != null) {
            ((Set) set).remove(uuid);
        }
    }

    private String getJoin(final List<String> strings) {
        return StringUtils.join(strings, ",").toLowerCase();
    }
}
