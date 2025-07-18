package ru.practicum.stats.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;


    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        boolean noUriFilter = (uris == null || uris.isEmpty());

        if (unique) {
            return noUriFilter
                    ? statsRepository.getStatsUniqueWithoutUriFilter(start, end)
                    : statsRepository.getStatsUnique(start, end, uris);
        } else {
            return noUriFilter
                    ? statsRepository.getStatsWithoutUriFilter(start, end)
                    : statsRepository.getStats(start, end, uris);
        }
    }
}