package ru.practicum.stats.hit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHitDto;


import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HitServiceImplTest {

    @Mock
    private HitRepository hitRepository;

    @InjectMocks
    private HitServiceImpl hitService;

    @Test
    void save_ShouldCallRepositorySave() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("svc")
                .uri("/events/1")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        hitService.save(dto);

        verify(hitRepository, times(1)).save(any());
    }
}