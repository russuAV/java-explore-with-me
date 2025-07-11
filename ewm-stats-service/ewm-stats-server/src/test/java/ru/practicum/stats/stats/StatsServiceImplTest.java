package ru.practicum.stats.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    void shouldCallGetStats_WhenUniqueIsFalse() {
        statsService.getStats(LocalDateTime.MIN, LocalDateTime.MAX, List.of("/a"), false);

        verify(statsRepository).getStats(any(), any(), any());
        verify(statsRepository, never()).getStatsUnique(any(), any(), any());
    }

    @Test
    void shouldCallGetStatsUnique_WhenUniqueIsTrue() {
        statsService.getStats(LocalDateTime.MIN, LocalDateTime.MAX, List.of("/a"), true);

        verify(statsRepository).getStatsUnique(any(), any(), any());
        verify(statsRepository, never()).getStats(any(), any(), any());
    }
}