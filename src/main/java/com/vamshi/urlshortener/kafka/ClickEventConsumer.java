package com.vamshi.urlshortener.kafka;

import com.vamshi.urlshortener.model.ClickEvent;
import com.vamshi.urlshortener.repository.ClickEventRepository;
import com.vamshi.urlshortener.repository.ShortUrlRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumes click events and persists them to PostgreSQL for durable analytics.
 */
@Component
public class ClickEventConsumer {

    private final ClickEventRepository clickEventRepository;
    private final ShortUrlRepository shortUrlRepository;

    public ClickEventConsumer(ClickEventRepository clickEventRepository,
                              ShortUrlRepository shortUrlRepository) {
        this.clickEventRepository = clickEventRepository;
        this.shortUrlRepository = shortUrlRepository;
    }

    @KafkaListener(topics = "${app.kafka.click-topic}", groupId = "click-analytics")
    @Transactional
    public void onClick(ClickMessage message) {
        clickEventRepository.save(new ClickEvent(
                message.shortCode(), message.clickedAt(), message.userAgent(), message.referrer()));
        shortUrlRepository.incrementClickCount(message.shortCode());
    }
}
