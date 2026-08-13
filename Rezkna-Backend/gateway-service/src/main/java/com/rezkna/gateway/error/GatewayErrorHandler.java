package com.rezkna.gateway.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring Cloud Gateway is WebFlux-based, so the MVC {@code @RestControllerAdvice} in
 * common-lib does not apply here. This replaces Spring Boot's default WebFlux error
 * handler (registered at order -1) so Gateway-originated errors - no matching route,
 * downstream failures - use the same {ok,message,data} envelope as every other service.
 */
@Component
@Order(-2)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatusCode statusCode = (ex instanceof ErrorResponse errorResponse)
                ? errorResponse.getStatusCode()
                : HttpStatus.INTERNAL_SERVER_ERROR;

        String message = (ex instanceof ErrorResponse errorResponse && errorResponse.getBody().getDetail() != null)
                ? errorResponse.getBody().getDetail()
                : "An unexpected gateway error occurred";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("message", message);
        body.put("data", null);

        exchange.getResponse().setStatusCode(statusCode);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception serializationFailure) {
            bytes = "{\"ok\":false,\"message\":\"An unexpected gateway error occurred\",\"data\":null}"
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
