package ru.practicum.stats.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StatsServiceIntegrationTest {

    @Autowired
    private StatsService statsService;

    @Test
    void shouldSaveAndGetStats() {
        EndpointHitDto hit = new EndpointHitDto(null, "ewm-main-service", "/test", "192.163.0.1", "2022-09-06 11:00:23");
        statsService.addHit(hit);
        List<ViewStatsDto> stats = statsService.getStats("2022-09-06 11:00:00", "2022-09-06 12:00:00", List.of("/test"), false);
        assertFalse(stats.isEmpty(), "Список статистики не должен быть пустым");
        assertEquals(1, stats.get(0).getHits(), "Количество хитов должно быть 1");
        assertEquals("/test", stats.get(0).getUri(), "URI должен совпадать");
    }
}