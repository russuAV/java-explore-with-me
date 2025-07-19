package ru.practicum.event.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.category.model.Category;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.*;
import ru.practicum.event.model.state.EventState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EventMapperTest {

    @Autowired
    private EventMapper mapper;

    @Test
    void toEvent_shouldMapNewEventDtoToEvent() {
        NewEventDto dto = NewEventDto.builder()
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Full Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .location(new Location(50.0, 30.0))
                .build();

        Event event = mapper.toEvent(dto);

        assertThat(event.getTitle()).isEqualTo(dto.getTitle());
        assertThat(event.getAnnotation()).isEqualTo(dto.getAnnotation());
        assertThat(event.getLocation()).isEqualTo(dto.getLocation());
    }

    @Test
    void toEventFullDto_shouldMapAllFields() {
        Event event = Event.builder()
                .id(1L)
                .title("Full Event")
                .annotation("Ann")
                .description("Desc")
                .eventDate(LocalDateTime.now().plusDays(2))
                .createdOn(LocalDateTime.now())
                .initiator(new User(1L, "user@example.com", "User"))
                .location(new Location(10.0, 20.0))
                .state(EventState.PUBLISHED)
                .paid(false)
                .category(new Category(1L, "Movie"))
                .views(123)
                .build();

        EventFullDto dto = mapper.toEventFullDto(event);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Full Event");
        assertThat(dto.getViews()).isEqualTo(123);
    }

    @Test
    void updateEventFromDto_shouldUpdateNonNullFieldsOnly() {
        Event event = new Event();
        event.setTitle("Original Title");
        event.setDescription("Original Description");

        UpdateEventUserRequest dto = UpdateEventUserRequest.builder()
                .title("Updated Title")
                .build();

        mapper.updateEventFromDto(dto, event);

        assertThat(event.getTitle()).isEqualTo("Updated Title");
        assertThat(event.getDescription()).isEqualTo("Original Description"); // не затирается null-ом
    }

    @Test
    void updateEventFromAdminDto_shouldUpdateNonNullFieldsOnly() {
        Event event = new Event();
        event.setTitle("Admin Title");

        UpdateEventAdminRequest dto = UpdateEventAdminRequest.builder()
                .annotation("Admin Updated")
                .build();

        mapper.updateEventFromAdminDto(dto, event);

        assertThat(event.getAnnotation()).isEqualTo("Admin Updated");
        assertThat(event.getTitle()).isEqualTo("Admin Title");
    }
}