package com.vamshi.urlshortener.service;

import com.vamshi.urlshortener.model.ShortUrl;
import com.vamshi.urlshortener.repository.ShortUrlRepository;
import com.vamshi.urlshortener.util.Base62;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    ShortUrlRepository repository;

    @InjectMocks
    UrlService service;

    @Test
    void shortenDerivesCodeFromGeneratedId() {
        when(repository.save(any(ShortUrl.class))).thenAnswer(inv -> {
            ShortUrl url = inv.getArgument(0);
            ReflectionTestUtils.setField(url, "id", 12345L);
            return url;
        });

        ShortUrl saved = service.shorten("https://example.com");

        assertEquals(Base62.encode(12345L), saved.getShortCode());
        assertEquals("https://example.com", saved.getLongUrl());
    }

    @Test
    void resolveReturnsLongUrlWhenFound() {
        when(repository.findByShortCode("abc1234")).thenReturn(Optional.of(new ShortUrl("https://example.com")));

        assertEquals("https://example.com", service.resolve("abc1234"));
    }

    @Test
    void resolveReturnsNullWhenMissing() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertNull(service.resolve("missing"));
    }

    @Test
    void statsThrowsWhenCodeUnknown() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> service.getStats("missing"));
    }
}
