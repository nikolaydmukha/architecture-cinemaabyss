package ru.cinemaabys.events.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinemaabys.events.service.EventProducer;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;

    public EventController(EventProducer eventProducer,
                           ObjectMapper objectMapper) {
        this.eventProducer = eventProducer;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":true}");
    }


    @PostMapping("/movie")
    public ResponseEntity<?> createMovieEvent(@RequestBody JsonNode request) {
        String[] requiredFields = {
                "movie_id", "title", "action", "user_id"
        };

        return publishEvent("Movie", request, requiredFields);
    }

    @PostMapping("/payment")
    public ResponseEntity<?> createPaymentEvent(@RequestBody JsonNode request) {
        String[] requiredFields = {
                "payment_id", "user_id", "amount",
                "status", "timestamp", "method_type"
        };

        return publishEvent("Payment", request, requiredFields);
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUserEvent(@RequestBody JsonNode request) {
        String[] requiredFields = {
                "user_id", "username", "action", "timestamp"
        };

        return publishEvent("User", request, requiredFields);
    }

    private ResponseEntity<?> publishEvent(String eventType,
                                           JsonNode request,
                                           String[] requiredFields) {
        for (String field : requiredFields) {
            if (!request.has(field) || request.get(field).isNull()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "error",
                                "Missing required field: " + field
                        )
                );
            }
        }

        try {
            String payload = objectMapper.writeValueAsString(request);

            eventProducer.publish(eventType, payload);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("{\n" +
                            "  \"status\": \"success\"\n" +
                            "}");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Failed to publish event"
                    ));
        }
    }
}
