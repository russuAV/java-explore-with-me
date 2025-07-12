package ru.practicum.stats.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.hit.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    // С фильтром по URI
    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(e))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND e.uri IN :uris
        GROUP BY e.app, e.uri
        ORDER BY COUNT(e) DESC
        """)
    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    // Без фильтра по URI
    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(e))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
        GROUP BY e.app, e.uri
        ORDER BY COUNT(e) DESC
        """)
    List<ViewStatsDto> getStatsWithoutUriFilter(LocalDateTime start, LocalDateTime end);

    // С фильтром по URI (уникальные IP)
    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND e.uri IN :uris
        GROUP BY e.app, e.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
        """)
    List<ViewStatsDto> getStatsUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    // Без фильтра по URI (уникальные IP)
    @Query("""
        SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
        GROUP BY e.app, e.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
        """)
    List<ViewStatsDto> getStatsUniqueWithoutUriFilter(LocalDateTime start, LocalDateTime end);
}