package com.andah.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bypass")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BypassController {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public BypassController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API Gateway bypass is working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-service/{path}")
    public Mono<ResponseEntity<String>> proxyUserService(@PathVariable String path, @RequestHeader Map<String, String> headers) {
        return proxyRequest("user-service", path, headers, null, HttpMethod.GET);
    }

    @PostMapping("/user-service/{path}")
    public Mono<ResponseEntity<String>> proxyUserServicePost(@PathVariable String path, @RequestHeader Map<String, String> headers, @RequestBody String body) {
        return proxyRequest("user-service", path, headers, body, HttpMethod.POST);
    }

    @GetMapping("/payment-service/{path}")
    public Mono<ResponseEntity<String>> proxyPaymentService(@PathVariable String path, @RequestHeader Map<String, String> headers) {
        return proxyRequest("payment-service", path, headers, null, HttpMethod.GET);
    }

    @PostMapping("/payment-service/{path}")
    public Mono<ResponseEntity<String>> proxyPaymentServicePost(@PathVariable String path, @RequestHeader Map<String, String> headers, @RequestBody String body) {
        return proxyRequest("payment-service", path, headers, body, HttpMethod.POST);
    }

    private Mono<ResponseEntity<String>> proxyRequest(String serviceName, String path, Map<String, String> headers, String body, HttpMethod method) {
        WebClient.RequestHeadersSpec<?> requestSpec;
        String url = "http://" + serviceName + ":8080/api/" + path;

        if (method == HttpMethod.GET) {
            requestSpec = webClientBuilder.build().get().uri(url);
        } else {
            requestSpec = webClientBuilder.build().post().uri(url).bodyValue(body != null ? body : "");
        }

        // Add headers except host
        headers.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("host")) {
                requestSpec.header(key, value);
            }
        });

        // Always add Authorization token for testing
        if (!headers.containsKey("Authorization")) {
            requestSpec.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlVTRVIiLCJ1c2VySWQiOiI2ODA5ZTgwMzRhMWNmMDQ1ZmEwNTU1ZjAiLCJlbWFpbCI6Im5paGVkQGdtYWlsLmNvbSIsInN1YiI6IjY4MDllODAzNGExY2YwNDVmYTA1NTVmMCIsImlhdCI6MTc0NTQ4MzYyMywiZXhwIjoxNzQ1NTcwMDIzfQ.c8XmLvdoy09fBeKEKR2isXZLi6yPFN-L6WWNomi3M5w");
        }

        return requestSpec.exchangeToMono(response -> {
            if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(String.class)
                    .map(responseBody -> ResponseEntity.ok(responseBody));
            } else {
                return response.bodyToMono(String.class)
                    .map(responseBody -> ResponseEntity
                        .status(response.statusCode())
                        .body(responseBody));
            }
        }).onErrorResume(e -> {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("service", serviceName);
            errorResponse.put("path", path);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse.toString()));
        });
    }

    enum HttpMethod {
        GET, POST
    }
}
