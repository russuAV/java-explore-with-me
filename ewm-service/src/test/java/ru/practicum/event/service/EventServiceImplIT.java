package ru.practicum.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.*;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.model.state.UpdateEventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class EventServiceImplIT {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;

    private User user;
    private Category category;

    @BeforeEach
    void setup() {
        user = userRepository.save(new User(null, "email@mail.ru", "TestUser"));
        category = categoryRepository.save(new Category(null, "Concert"));
    }

    @Test
    void create_shouldSaveEventCorrectly() {
        NewEventDto dto = NewEventDto.builder()
                .title("Spring Fest")
                .annotation("Odio sint delectus beatae nulla")
                .description("Odio sint delectus beatae nulla")
                .eventDate(LocalDateTime.now().plusDays(5))
                .location(new Location(55.0, 37.0))
                .paid(true)
                .participantLimit(300)
                .requestModeration(true)
                .category(category.getId())
                .build();

        EventFullDto created = eventService.create(user.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Spring Fest");
        assertThat(created.getCategory().getId()).isEqualTo(category.getId());
        assertThat(created.getInitiator().getId()).isEqualTo(user.getId());
        assertThat(created.getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void create_shouldThrowBadRequest_whenEventDateLessThan2HoursAhead() {
        NewEventDto dto = NewEventDto.builder()
                .title("Invalid Event")
                .annotation("A".repeat(30))
                .description("D".repeat(30))
                .eventDate(LocalDateTime.now().plusMinutes(90)) // < 2 часов
                .location(new Location(1.0, 1.0))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .category(category.getId())
                .build();

        assertThatThrownBy(() -> eventService.create(user.getId(), dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Событие не удовлетворяет правилам");
    }

    @Test
    void updateByInitiator_shouldUpdateFields() {
        Event event = createEvent("Initial");
        UpdateEventUserRequest dto = UpdateEventUserRequest.builder()
                .title("Updated Title")
                .annotation("Updated Ann")
                .description("Updated Desc")
                .build();

        EventFullDto updated = eventService.updateByInitiator(user.getId(), event.getId(), dto);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getAnnotation()).isEqualTo("Updated Ann");
        assertThat(updated.getDescription()).isEqualTo("Updated Desc");
    }

    @Test
    void updateByInitiator_shouldThrow_ifNotOwner() {
        Event event = createEvent("Foreign Event");
        User other = userRepository.save(new User(null, "other@mail.com", "Other"));
        UpdateEventUserRequest dto = UpdateEventUserRequest.builder().title("Hack").build();

        assertThatThrownBy(() -> eventService.updateByInitiator(other.getId(), event.getId(), dto))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateByInitiator_shouldThrow_ifAlreadyPublished() {
        Event event = createEvent("Published");
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventUserRequest dto = UpdateEventUserRequest.builder().title("Attempt").build();

        assertThatThrownBy(() -> eventService.updateByInitiator(user.getId(), event.getId(), dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateByAdmin_shouldThrow_ifAlreadyPublished() {
        Event event = createEvent("AlreadyPub");
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventAdminRequest dto = UpdateEventAdminRequest.builder()
                .stateAction(UpdateEventState.PUBLISH_EVENT)
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> eventService.updateByAdmin(event.getId(), dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateByAdmin_shouldThrow_ifCanceled() {
        Event event = createEvent("Canceled");
        event.setState(EventState.CANCELED);
        eventRepository.save(event);

        UpdateEventAdminRequest dto = UpdateEventAdminRequest.builder()
                .stateAction(UpdateEventState.PUBLISH_EVENT)
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> eventService.updateByAdmin(event.getId(), dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateByAdmin_shouldThrow_ifPublishRejectedState() {
        Event event = createEvent("NotPending");
        event.setState(EventState.CANCELED);
        eventRepository.save(event);

        UpdateEventAdminRequest dto = UpdateEventAdminRequest.builder()
                .stateAction(UpdateEventState.PUBLISH_EVENT)
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> eventService.updateByAdmin(event.getId(), dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateByAdmin_shouldThrow_ifRejectPublished() {
        Event event = createEvent("RejectAttempt");
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventAdminRequest dto = UpdateEventAdminRequest.builder()
                .stateAction(UpdateEventState.REJECT_EVENT)
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> eventService.updateByAdmin(event.getId(), dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateByAdmin_shouldThrow_ifEventDateTooSoon() {
        Event event = createEvent("SoonUpdate");
        UpdateEventAdminRequest dto = UpdateEventAdminRequest.builder()
                .eventDate(LocalDateTime.now().plusMinutes(30))
                .build();

        assertThatThrownBy(() -> eventService.updateByAdmin(event.getId(), dto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getEventById_shouldThrow_ifNotOwner() {
        Event event = createEvent("Owner");
        User stranger = userRepository.save(new User(null, "stranger@mail.com", "Stranger"));

        assertThatThrownBy(() -> eventService.getEventById(stranger.getId(), event.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateByAdmin_shouldPublishEvent() {
        Event event = createEvent("AdminEvent");

        UpdateEventAdminRequest dto = UpdateEventAdminRequest.builder()
                .stateAction(UpdateEventState.PUBLISH_EVENT)
                .eventDate(LocalDateTime.now().plusDays(2))
                .build();

        EventFullDto result = eventService.updateByAdmin(event.getId(), dto);

        assertThat(result.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(result.getPublishedOn()).isNotNull();
    }

    @Test
    void getUserEvents_shouldReturnCreated() {
        createEvent("Event1");
        createEvent("Event2");

        List<EventShortDto> events = eventService.getUserEvents(user.getId(), 0, 10);

        assertThat(events).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void getEventById_shouldReturnCorrectEvent() {
        Event event = createEvent("MyEvent");

        EventFullDto dto = eventService.getEventById(user.getId(), event.getId());

        assertThat(dto.getId()).isEqualTo(event.getId());
        assertThat(dto.getTitle()).isEqualTo("MyEvent");
    }

    @Test
    void getEventsByParams_shouldReturnMatching() {
        Event event = createEvent("MultiParams");

        AdminEventSearchRequest request = AdminEventSearchRequest.builder()
                .users(List.of(user.getId()))
                .states(List.of("PENDING"))
                .categories(List.of(category.getId()))
                .from(0).size(10)
                .build();

        List<EventFullDto> list = eventService.getEventsByParams(request);
        assertThat(list).extracting(EventFullDto::getId).contains(event.getId());
    }

    @Test
    void findAllById_shouldReturnAllFound() {
        Event e1 = createEvent("Event1");
        Event e2 = createEvent("Event2");

        Set<Event> result = eventService.findAllById(Set.of(e1.getId(), e2.getId()));

        assertThat(result).hasSize(2);
    }

    @Test
    void incrementConfirmedRequests_shouldIncrease() {
        Event event = createEvent("ConfirmMe");

        eventService.incrementConfirmedRequests(event.getId(), 2);

        Event updated = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getConfirmedRequests()).isEqualTo(2);
    }

    @Test
    void decrementConfirmedRequests_shouldDecrease() {
        Event event = createEvent("Decrement");
        event.setConfirmedRequests(3);
        eventRepository.save(event);

        eventService.decrementConfirmedRequests(event.getId());

        Event updated = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getConfirmedRequests()).isEqualTo(2);
    }

    private Event createEvent(String title) {
        Event event = Event.builder()
                .title(title)
                .annotation("Odio sint delectus beatae nulla")
                .description("Odio sint delectus beatae nulla")
                .eventDate(LocalDateTime.now().plusDays(2))
                .location(new Location(1.0, 2.0))
                .initiator(user)
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .category(category)
                .confirmedRequests(0)
                .paid(false)
                .requestModeration(false)
                .participantLimit(100)
                .build();
        return eventRepository.save(event);
    }
}
