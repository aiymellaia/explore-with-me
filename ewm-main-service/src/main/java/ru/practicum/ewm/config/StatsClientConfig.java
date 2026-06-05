package ru.practicum.ewm.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClient statsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        return new StatsClient(serverUrl, builder);
    }
}