package ru.cinemaabys.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic}")
    public void consume(String payload) {
        log.info("EVENT CONSUMED | payload={}", payload);
    }
}