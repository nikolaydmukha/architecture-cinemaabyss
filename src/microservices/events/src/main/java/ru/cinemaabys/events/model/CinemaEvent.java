package ru.cinemaabys.events.model;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public record CinemaEvent(String eventId,
                          @NotBlank String eventType,
                          Object object,
                          Instant timestamp) {

    public static CinemaEvent create(String eventType,
                                     Object object) {

        return new CinemaEvent(
                UUID.randomUUID().toString(),
                eventType,
                object,
                Instant.now()
        );
    }
}