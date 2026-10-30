package ru.cinemaabys.proxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoutingService {

    @Value("${proxy.monolith-url}")
    private String monolithUrl;

    @Value("${proxy.movies-service-url}")
    private String moviesServiceUrl;

    @Value("${proxy.events-service-url}")
    private String eventsServiceUrl;

    @Value("${proxy.gradual-migration:true}")
    private boolean gradualMigration;

    @Value("${proxy.movies-migration-percent:0}")
    private int migrationPercent;

    public String resolveTarget(String path) {

        // События направляем в Events Service
        if (path.startsWith("/api/events")) {
            return eventsServiceUrl;
        }

        // Остальные запросы, кроме фильмов, идут в монолит
        if (!path.startsWith("/api/movies")) {
            return monolithUrl;
        }

        // Постепенная миграция отключена:
        // весь функционал фильмов обслуживает микросервис
        if (!gradualMigration) {
            return moviesServiceUrl;
        }

        // Защита от некорректных значений
        int percent = Math.max(0, Math.min(100, migrationPercent));

        // Случайное распределение запросов
        int random = ThreadLocalRandom.current().nextInt(100);

        return random < percent
                ? moviesServiceUrl
                : monolithUrl;
    }
}