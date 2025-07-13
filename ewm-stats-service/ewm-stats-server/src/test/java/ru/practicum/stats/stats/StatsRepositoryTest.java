package ru.practicum.stats.stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.hit.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StatsRepositoryTest {

    @Autowired
    private StatsRepository statsRepository;

    @Test
    void testGetStats() {
        statsRepository.save(new EndpointHit(
                null, "main-service", "/events", "192.168.0.1", LocalDateTime.now().minusHours(1)));
        statsRepository.save(new EndpointHit(
                null, "main-service", "/events", "192.168.0.2", LocalDateTime.now().minusHours(1)));
        statsRepository.save(new EndpointHit(
                null, "main-service", "/users", "192.168.0.3", LocalDateTime.now().minusHours(1)));

        List<ViewStatsDto> result = statsRepository.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/events", "/users")
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUri()).isEqualTo("/events");
        assertThat(result.get(0).getHits()).isEqualTo(2);
        assertThat(result.get(1).getUri()).isEqualTo("/users");
        assertThat(result.get(1).getHits()).isEqualTo(1);
    }

    @Test
    void testGetStatsUnique() {
        String uri = "/events";
        String app = "main-service";
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);

        statsRepository.save(new EndpointHit(null, app, uri, "192.168.0.1", timestamp));
        statsRepository.save(new EndpointHit(null, app, uri, "192.168.0.1", timestamp));
        statsRepository.save(new EndpointHit(null, app, uri, "192.168.0.2", timestamp));

        List<ViewStatsDto> result = statsRepository.getStatsUnique(
                timestamp.minusHours(1),
                timestamp.plusHours(1),
                List.of(uri)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUri()).isEqualTo(uri);
        assertThat(result.get(0).getHits()).isEqualTo(2);
    }
}