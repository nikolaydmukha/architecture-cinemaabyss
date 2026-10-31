package ru.cinemaabys.proxy.controller;

import ru.cinemaabys.proxy.service.RoutingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.net.URI;
import java.util.Set;

@RestController
public class ProxyController {

    private final WebClient webClient;
    private final RoutingService routingService;

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "host",
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade"
    );

    public ProxyController(WebClient webClient,
                           RoutingService routingService) {
        this.webClient = webClient;
        this.routingService = routingService;
    }

    @RequestMapping("/**")
    public Mono<ResponseEntity<byte[]>> proxy(ServerWebExchange exchange) {
        var request = exchange.getRequest();

        String path = request.getURI().getRawPath();
        String target = routingService.resolveTarget(path);

        String query = request.getURI().getRawQuery();

        String targetUrl = target + path + (query != null ? "?" + query : "");

        HttpMethod method = request.getMethod();

        if (method == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        WebClient.RequestBodySpec requestSpec = webClient
                .method(method)
                .uri(URI.create(targetUrl))
                .headers(headers -> request.getHeaders()
                        .forEach((name, values) -> {
                            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                                headers.put(name, values);
                            }
                        })
                );

        return requestSpec
                .body(BodyInserters.fromDataBuffers(request.getBody()))
                .exchangeToMono(response -> response.toEntity(byte[].class))
                .map(response -> {
                    HttpHeaders responseHeaders = new HttpHeaders();

                    response.getHeaders().forEach((name, values) -> {
                                if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                                    responseHeaders.put(name, values);
                                }
                            }
                    );

                    return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
                });
    }
}
