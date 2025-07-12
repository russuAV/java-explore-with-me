package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stats.server.url}")
    private String serverUrl;

    public void saveHit(EndpointHitDto hitDto) {
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto);
        restTemplate.postForEntity(serverUrl + "/hit", request, Void.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = encode(start);
        String endStr = encode(end);

        StringBuilder urlBuilder = new StringBuilder(serverUrl + "/stats?start=" + startStr + "&end=" + endStr);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                String encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8);
                urlBuilder.append("&uris=").append(encodedUri);
            }
        }

        urlBuilder.append("&unique=").append(unique);

        ResponseEntity<ViewStatsDto[]> response = restTemplate
                .getForEntity(urlBuilder.toString(), ViewStatsDto[].class);
        return List.of(response.getBody());
    }

    private String encode(LocalDateTime dateTime) {
        String raw = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}