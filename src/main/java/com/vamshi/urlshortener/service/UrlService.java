package com.vamshi.urlshortener.service;

import com.vamshi.urlshortener.model.ShortUrl;
import com.vamshi.urlshortener.repository.ShortUrlRepository;
import com.vamshi.urlshortener.util.Base62;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {

    private final ShortUrlRepository repository;

    public UrlService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    /** Saves the URL, then derives a unique base62 code from its generated ID (no collisions, no retries). */
    @Transactional
    public ShortUrl shorten(String longUrl) {
        ShortUrl saved = repository.save(new ShortUrl(longUrl));
        saved.setShortCode(Base62.encode(saved.getId()));
        return saved;
    }

    /** Read path: served from Redis after the first lookup, so hot links skip the database. */
    @Cacheable(cacheNames = "urls", key = "#code", unless = "#result == null")
    public String resolve(String code) {
        return repository.findByShortCode(code)
                .map(ShortUrl::getLongUrl)
                .orElse(null);
    }

    public ShortUrl getStats(String code) {
        return repository.findByShortCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));
    }
}
