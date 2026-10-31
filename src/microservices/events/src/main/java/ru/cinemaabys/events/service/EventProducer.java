package ru.cinemaabys.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public EventProducer(KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(String eventType, String payload) {
        try {
            kafkaTemplate
                    .send(topic, eventType, payload)
                    .get(10, TimeUnit.SECONDS);

            log.info(
                    "Event published to Kafka: topic={}, type={}, payload={}",
                    topic,
                    eventType,
                    payload
            );

        } catch (Exception e) {
            log.error(
                    "Failed to publish event: type={}",
                    eventType,
                    e
            );

            throw new IllegalStateException(
                    "Could not publish event to Kafka",
                    e
            );
        }
    }
}