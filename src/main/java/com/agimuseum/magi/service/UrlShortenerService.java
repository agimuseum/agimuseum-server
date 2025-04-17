package com.agimuseum.magi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to handle URLs that are too long for database storage
 */
@Service
@Slf4j
public class UrlShortenerService {

    // In-memory cache for URL mappings
    private final Map<String, String> urlCache = new ConcurrentHashMap<>();

    // Base URL for shortened URLs
    private static final String SHORT_URL_BASE = "/api/r/";

    // Maximum URL length to store in database
    private static final int MAX_URL_LENGTH = 900; // Leave some room below the 1024 limit

    /**
     * Shortens a URL if it's too long for database storage
     * @param originalUrl The original long URL
     * @return A shorter URL if needed, or the original if it's already short enough
     */
    public String shortenIfNeeded(String originalUrl) {
        if (originalUrl == null || originalUrl.length() <= MAX_URL_LENGTH) {
            return originalUrl; // No need to shorten
        }

        try {
            log.debug("URL too long for database storage ({} chars), shortening", originalUrl.length());

            // Generate a short key
            String key = generateShortKey();

            // Store the mapping
            urlCache.put(key, originalUrl);

            // Create a short URL
            String shortUrl = SHORT_URL_BASE + key;
            log.info("Created short URL: {} for long URL of length: {}", shortUrl, originalUrl.length());

            return shortUrl;
        } catch (Exception e) {
            log.error("Failed to shorten URL, returning original URL with potential truncation", e);
            // Fallback - return a truncated URL
            return originalUrl.substring(0, MAX_URL_LENGTH);
        }
    }

    /**
     * Gets the original URL from a shortened one
     * @param shortUrl The shortened URL
     * @return The original URL, or the input if not found
     */
    public String getOriginalUrl(String shortUrl) {
        if (shortUrl == null || !shortUrl.startsWith(SHORT_URL_BASE)) {
            return shortUrl; // Not a shortened URL
        }

        String key = shortUrl.substring(SHORT_URL_BASE.length());
        String originalUrl = urlCache.getOrDefault(key, shortUrl); // Return original or the input if not found

        log.debug("Retrieved original URL of length {} for short URL: {}",
                originalUrl.length(), shortUrl);

        return originalUrl;
    }

    /**
     * Extracts the key from a short URL
     * @param shortUrl The short URL
     * @return The key, or null if not a short URL
     */
    public String extractKeyFromShortUrl(String shortUrl) {
        if (shortUrl == null || !shortUrl.startsWith(SHORT_URL_BASE)) {
            return null;
        }

        String key = shortUrl.substring(SHORT_URL_BASE.length());
        log.debug("Extracted key: {} from short URL: {}", key, shortUrl);
        return key;
    }

    /**
     * Generate a short key for URL shortening
     * @return A unique short key
     */
    private String generateShortKey() {
        // Use UUID to generate a random key but take only the first 8 characters
        String key = UUID.randomUUID().toString().substring(0, 8);
        log.debug("Generated short key: {}", key);
        return key;
    }
}