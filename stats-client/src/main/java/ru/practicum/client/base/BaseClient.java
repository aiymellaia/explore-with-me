package ru.practicum.client.base;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> post(String path, Object body) {
        return rest.postForEntity(path, new HttpEntity<>(body), Object.class);
    }

    protected ResponseEntity<Object> get(String path, Map<String, Object> parameters) {
        return rest.getForEntity(path, Object.class, parameters);
    }
}