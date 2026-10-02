package com.vamshi.urlshortener.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes click events asynchronously so the redirect path never waits on analytics writes.
 */
@Component
public class ClickEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ClickEventProducer.class);

    private final KafkaTemplate<String, ClickMessage> kafkaTemplate;
    private final String topic;

    public ClickEventProducer(KafkaTemplate<String, ClickMessage> kafkaTemplate,
                              @Value("${app.kafka.click-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(ClickMessage message) {
        // Keyed by short code so all clicks for one link land on the same partition (ordered).
        kafkaTemplate.send(topic, message.shortCode(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) log.warn("Failed to publish click for {}", message.shortCode(), ex);
                });
    }
}
