package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.dto.ViewStatsDto;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM EndpointHit h WHERE h.timestamp BETWEEN ?1 AND ?2 AND (h.uri IN ?3 OR ?3 IS NULL) " +
            "GROUP BY h.app, h.uri ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStatsDto> getStatsUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT(h.ip)) " +
            "FROM EndpointHit h WHERE h.timestamp BETWEEN ?1 AND ?2 AND (h.uri IN ?3 OR ?3 IS NULL) " +
            "GROUP BY h.app, h.uri ORDER BY COUNT(h.ip) DESC")
    List<ViewStatsDto> getStatsAll(LocalDateTime start, LocalDateTime end, List<String> uris);
}