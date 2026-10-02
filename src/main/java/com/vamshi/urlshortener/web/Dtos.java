package com.vamshi.urlshortener.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record ShortenRequest(
            @NotBlank
            @Pattern(regexp = "^https?://.+", message = "must start with http:// or https://")
            String url) {}

    public record ShortenResponse(String code, String shortUrl, String longUrl) {}

    public record RecentClick(Instant clickedAt, String userAgent, String referrer) {}

    public record StatsResponse(String code, String longUrl, Instant createdAt,
                                long totalClicks, List<RecentClick> recentClicks) {}
}
