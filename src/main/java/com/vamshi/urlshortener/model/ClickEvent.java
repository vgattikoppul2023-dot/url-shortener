package com.vamshi.urlshortener.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "click_events", indexes = @Index(name = "idx_click_code", columnList = "shortCode"))
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String shortCode;

    @Column(nullable = false)
    private Instant clickedAt;

    @Column(length = 512)
    private String userAgent;

    @Column(length = 2048)
    private String referrer;

    protected ClickEvent() {}

    public ClickEvent(String shortCode, Instant clickedAt, String userAgent, String referrer) {
        this.shortCode = shortCode;
        this.clickedAt = clickedAt;
        this.userAgent = userAgent;
        this.referrer = referrer;
    }

    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public Instant getClickedAt() { return clickedAt; }
    public String getUserAgent() { return userAgent; }
    public String getReferrer() { return referrer; }
}
