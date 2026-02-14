package com.clinica.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> authFallback(ServerWebExchange exchange) {
        return buildFallback("ms-auth", exchange);
    }

    @GetMapping(value = "/historia", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> historiaFallback(ServerWebExchange exchange) {
        return buildFallback("ms-historia-clinica", exchange);
    }

    @GetMapping(value = "/transcripcion", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> transcripcionFallback(ServerWebExchange exchange) {
        return buildFallback("ms-transcripcion", exchange);
    }

    @GetMapping(value = "/auditoria", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> auditoriaFallback(ServerWebExchange exchange) {
        return buildFallback("ms-auditoria", exchange);
    }

    @GetMapping(value = "/exportacion", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> exportacionFallback(ServerWebExchange exchange) {
        return buildFallback("ms-exportacion", exchange);
    }

    private Mono<Map<String, Object>> buildFallback(String servicio, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(Map.of(
                "status", 503,
                "error", "Service Unavailable",
                "message", "El servicio " + servicio + " no esta disponible en este momento. Intente nuevamente."
        ));
    }
}
