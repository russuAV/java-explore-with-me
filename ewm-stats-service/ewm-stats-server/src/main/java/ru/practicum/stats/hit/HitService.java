package ru.practicum.stats.hit;

import ru.practicum.dto.EndpointHitDto;

public interface HitService {
    void save(EndpointHitDto hitDto);
}