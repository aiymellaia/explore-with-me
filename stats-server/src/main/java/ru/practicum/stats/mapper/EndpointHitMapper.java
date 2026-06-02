package ru.practicum.stats.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.stats.model.EndpointHit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EndpointHitMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit toEndpointHit(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .createTs(LocalDateTime.parse(dto.getTimestamp(), formatter))
                .build();
    }
}