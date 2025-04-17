package com.agimuseum.magi.controller;

import com.agimuseum.magi.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

/**
 * Controller for handling URL redirections
 */
@Controller
@RequestMapping("/api/r")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final UrlShortenerService urlShortenerService;

    /**
     * Redirects short URLs to their original targets
     */
    @GetMapping("/{key}")
    public ResponseEntity<Void> redirect(@PathVariable String key) {
        String shortUrl = "/api/r/" + key;
        String longUrl = urlShortenerService.getOriginalUrl(shortUrl);

        if (longUrl.equals(shortUrl)) {
            // Not found in cache
            log.warn("No mapping found for short URL: {}", shortUrl);
            return ResponseEntity.notFound().build();
        }

        log.info("Redirecting short URL {} to original URL", shortUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }
}