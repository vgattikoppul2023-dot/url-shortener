package com.vamshi.urlshortener.web;

import com.vamshi.urlshortener.kafka.ClickEventProducer;
import com.vamshi.urlshortener.kafka.ClickMessage;
import com.vamshi.urlshortener.model.ShortUrl;
import com.vamshi.urlshortener.repository.ClickEventRepository;
import com.vamshi.urlshortener.service.UrlNotFoundException;
import com.vamshi.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final ClickEventProducer clickProducer;
    private final ClickEventRepository clickRepository;
    private final String baseUrl;

    public UrlController(UrlService urlService,
                         ClickEventProducer clickProducer,
                         ClickEventRepository clickRepository,
                         @Value("${app.base-url}") String baseUrl) {
        this.urlService = urlService;
        this.clickProducer = clickProducer;
        this.clickRepository = clickRepository;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<Dtos.ShortenResponse> shorten(@Valid @RequestBody Dtos.ShortenRequest request) {
        ShortUrl saved = urlService.shorten(request.url());
        String code = saved.getShortCode();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Dtos.ShortenResponse(code, baseUrl + "/" + code, saved.getLongUrl()));
    }

    @GetMapping("/{code:[0-9a-zA-Z]{1,16}}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        String longUrl = urlService.resolve(code);
        if (longUrl == null) throw new UrlNotFoundException(code);

        clickProducer.publish(new ClickMessage(code, Instant.now(),
                request.getHeader(HttpHeaders.USER_AGENT),
                request.getHeader(HttpHeaders.REFERER)));

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();
    }

    @GetMapping("/api/urls/{code}/stats")
    public Dtos.StatsResponse stats(@PathVariable String code) {
        ShortUrl url = urlService.getStats(code);
        var recent = clickRepository.findTop10ByShortCodeOrderByClickedAtDesc(code).stream()
                .map(c -> new Dtos.RecentClick(c.getClickedAt(), c.getUserAgent(), c.getReferrer()))
                .toList();
        return new Dtos.StatsResponse(code, url.getLongUrl(), url.getCreatedAt(), url.getClickCount(), recent);
    }
}
