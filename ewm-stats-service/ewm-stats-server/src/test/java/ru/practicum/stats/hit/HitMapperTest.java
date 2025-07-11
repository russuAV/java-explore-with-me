package ru.practicum.stats.hit;
import org.junit.jupiter.api.Test;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HitMapperTest {

    @Test
    void toEntity_ShouldCopyAllFields() {
        LocalDateTime ts = LocalDateTime.of(2025, 7, 11, 13, 0);
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("my-app")
                .uri("/test/1")
                .ip("10.0.0.1")
                .timestamp(ts)
                .build();

        EndpointHit entity = HitMapper.toEntity(dto);

        assertEquals(dto.getApp(),       entity.getApp());
        assertEquals(dto.getUri(),       entity.getUri());
        assertEquals(dto.getIp(),        entity.getIp());
        assertEquals(dto.getTimestamp(), entity.getTimestamp());
    }
}