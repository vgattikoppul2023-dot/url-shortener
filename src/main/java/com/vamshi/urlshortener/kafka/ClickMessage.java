package com.vamshi.urlshortener.kafka;

import java.time.Instant;

/** Event published to Kafka every time a short link is visited. */
public record ClickMessage(String shortCode, Instant clickedAt, String userAgent, String referrer) {}
