package ru.practicum.stats.hit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;

@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Override
    public void save(EndpointHitDto hitDto) {
        hitRepository.save(HitMapper.toEntity(hitDto));
    }
}