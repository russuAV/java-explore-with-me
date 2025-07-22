package ru.practicum.event.participation.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.event.model.Event;
import ru.practicum.event.participation.model.ParticipationRequest;
import ru.practicum.event.participation.model.ParticipationRequestDto;
import ru.practicum.event.participation.model.RequestState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipationRequestMapperTest {

    private ParticipationRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ParticipationRequestMapper.class);
    }

    @Test
    void toDto_shouldMapAllFields() {
        Event event = Event.builder()
                .id(42L)
                .title("Test Event")
                .build();

        User user = new User(7L, "test@example.com", "TestUser");

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestState.CONFIRMED)
                .build();

        ParticipationRequestDto dto = mapper.toDto(request);

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
        assertThat(dto.getEvent()).isEqualTo(event.getId());
        assertThat(dto.getRequester()).isEqualTo(user.getId());
        assertThat(dto.getStatus()).isEqualTo(RequestState.CONFIRMED);
    }
}