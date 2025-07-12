    package ru.practicum.stats.stats;

    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.boot.test.web.client.TestRestTemplate;
    import org.springframework.http.HttpEntity;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.test.context.ActiveProfiles;
    import ru.practicum.dto.EndpointHitDto;
    import ru.practicum.stats.StatsServerApplication;

    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.Map;

    import static org.assertj.core.api.Assertions.assertThat;

    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            classes = StatsServerApplication.class
    )
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    @ActiveProfiles("test")
    public class StatsControllerIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Test
        void postHit_whenMissingField_returns400() {
            Map<String, String> invalidHit = Map.of(
                    "app", "test-app",
                    "uri", "/test",
                    "ip", "127.0.0.1"
                    // Нет timestamp
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/hit",
                    new HttpEntity<>(invalidHit),
                    Map.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsKey("timestamp");
            assertThat(response.getBody().get("timestamp").toString().toLowerCase()).contains("null");
        }

        @Test
        void getStats_shouldReturnCorrectCounts() {
            LocalDateTime now = LocalDateTime.now();
            createHit("app1", "/events/1", "192.168.1.1", now.minusHours(2));
            createHit("app1", "/events/1", "192.168.1.2", now.minusHours(1));

            String url = String.format(
                    "/stats?start=%s&end=%s",
                    now.minusDays(1).format(formatter),
                    now.plusDays(1).format(formatter)
            );

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .contains("\"uri\":\"/events/1\"")
                    .contains("\"hits\":2");
        }

        @Test
        void getStatsUnique_shouldCountDistinctIps() {
            LocalDateTime now = LocalDateTime.now();

            createHit("app1", "/events/1", "192.168.1.1", now.minusHours(2));
            createHit("app1", "/events/1", "192.168.1.1", now.minusHours(1));
            createHit("app1", "/events/1", "192.168.1.2", now.minusMinutes(30));

            String url = String.format(
                    "/stats?start=%s&end=%s&uris=/events/1&unique=true",
                    now.minusDays(1).format(formatter),
                    now.plusDays(1).format(formatter)
            );

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .contains("\"uri\":\"/events/1\"")
                    .contains("\"hits\":2");
        }

        private void createHit(String app, String uri, String ip, LocalDateTime timestamp) {
            EndpointHitDto dto = new EndpointHitDto();
            dto.setApp(app);
            dto.setUri(uri);
            dto.setIp(ip);
            dto.setTimestamp(timestamp);
            restTemplate.postForEntity("/hit", dto, Void.class);
        }
    }