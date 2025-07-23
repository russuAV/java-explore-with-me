package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.model.state.UpdateEventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServiceImplTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserService userService;
    @Mock private EventMapper eventMapper;
    @Mock private CategoryService categoryService;
    @Mock private StatsClient statsClient;
    @Mock private HttpServletRequest request;

    @InjectMocks private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldThrowBadRequest_whenEventDateTooSoon() {
        Long userId = 1L;
        NewEventDto dto = NewEventDto.builder()
                .eventDate(LocalDateTime.now().plusMinutes(30))
                .build();

        assertThrows(BadRequestException.class, () -> eventService.create(userId, dto));
    }

    @Test
    void create_shouldCreateEventSuccessfully() {
        Long userId = 1L;
        NewEventDto dto = NewEventDto.builder()
                .title("Test")
                .annotation("Ann")
                .description("Desc")
                .eventDate(LocalDateTime.now().plusDays(1))
                .category(2L)
                .build();

        User user = new User(userId, "mail@mail.com", "Name");
        Event mappedEvent = new Event();
        Event savedEvent = new Event();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(eventMapper.toEvent(dto)).thenReturn(mappedEvent);
        when(categoryService.getEntityById(dto.getCategory())).thenReturn(new Category());
        when(eventRepository.save(any())).thenReturn(savedEvent);
        when(eventMapper.toEventFullDto(any())).thenReturn(new EventFullDto());

        EventFullDto result = eventService.create(userId, dto);

        assertThat(result).isNotNull();
        verify(eventRepository).save(mappedEvent);
    }

    @Test
    void updateByInitiator_shouldThrowForbidden_ifUserIsNotInitiator() {
        Long userId = 1L;
        Long eventId = 2L;
        Event event = new Event();
        event.setInitiator(new User(999L, "email", "name"));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenException.class, () ->
                eventService.updateByInitiator(userId, eventId, new UpdateEventUserRequest()));
    }

    @Test
    void updateByInitiator_shouldThrowBadRequest_ifInvalidDate() {
        Long userId = 1L;
        Long eventId = 2L;
        Event event = new Event();
        event.setInitiator(new User(userId, "email", "name"));
        event.setState(EventState.PENDING);

        UpdateEventUserRequest dto = new UpdateEventUserRequest();
        dto.setEventDate(LocalDateTime.now().plusMinutes(30));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.getEntityById(userId)).thenReturn(new User(userId, "email", "name"));

        assertThrows(BadRequestException.class, () ->
                eventService.updateByInitiator(userId, eventId, dto));
    }

    @Test
    void updateByInitiator_shouldThrowConflict_ifEventPublished() {
        Long userId = 1L;
        Long eventId = 2L;
        Event event = new Event();
        event.setInitiator(new User(userId, "email", "name"));
        event.setState(EventState.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.getEntityById(userId)).thenReturn(new User(userId, "email", "name"));

        assertThrows(ConflictException.class, () ->
                eventService.updateByInitiator(userId, eventId, new UpdateEventUserRequest()));
    }

    @Test
    void updateByAdmin_shouldThrowConflict_ifCanceled() {
        Event event = new Event();
        event.setState(EventState.CANCELED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () ->
                eventService.updateByAdmin(1L, new UpdateEventAdminRequest()));
    }

    @Test
    void updateByAdmin_shouldThrowConflict_ifAlreadyPublished() {
        Event event = new Event();
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () ->
                eventService.updateByAdmin(1L, new UpdateEventAdminRequest()));
    }

    @Test
    void updateByAdmin_shouldThrowBadRequest_ifEventDateTooSoon() {
        Event event = new Event();
        event.setState(EventState.PENDING);

        UpdateEventAdminRequest dto = new UpdateEventAdminRequest();
        dto.setEventDate(LocalDateTime.now().plusMinutes(30));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(BadRequestException.class, () ->
                eventService.updateByAdmin(1L, dto));
    }

    @Test
    void updateByAdmin_shouldThrowConflict_ifPublishNonPending() {
        Event event = new Event();
        event.setState(EventState.CANCELED);
        UpdateEventAdminRequest dto = new UpdateEventAdminRequest();
        dto.setStateAction(UpdateEventState.PUBLISH_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () ->
                eventService.updateByAdmin(1L, dto));
    }

    @Test
    void updateByAdmin_shouldThrowConflict_ifRejectPublished() {
        Event event = new Event();
        event.setState(EventState.PUBLISHED);
        UpdateEventAdminRequest dto = new UpdateEventAdminRequest();
        dto.setStateAction(UpdateEventState.REJECT_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () ->
                eventService.updateByAdmin(1L, dto));
    }

    @Test
    void getUserEvents_shouldReturnMappedList() {
        Long userId = 1L;
        Event event = new Event();
        EventShortDto dto = new EventShortDto();
        int from = 0;
        int size = 10;

        when(userService.getEntityById(userId)).thenReturn(new User());
        when(eventRepository.findUserEventsWithOffset(userId, from, size)).thenReturn(List.of(event));
        when(eventMapper.toEventShortDto(event)).thenReturn(dto);

        List<EventShortDto> result = eventService.getUserEvents(userId, from, size);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
        verify(eventRepository).findUserEventsWithOffset(userId, from, size);
    }

    @Test
    void getEntityById_shouldReturnEventIfExists() {
        Event event = new Event();
        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        assertThat(eventService.getEntityById(5L)).isEqualTo(event);
    }

    @Test
    void findAllById_shouldReturnSetOfEvents() {
        Event e = new Event();
        when(eventRepository.findByIdIn(Set.of(1L, 2L))).thenReturn(Set.of(e));
        Set<Event> result = eventService.findAllById(Set.of(1L, 2L));
        assertThat(result).containsExactly(e);
    }

    @Test
    void incrementConfirmedRequests_shouldIncreaseValue() {
        Event e = new Event();
        e.setConfirmedRequests(2);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

        eventService.incrementConfirmedRequests(1L, 3);

        assertThat(e.getConfirmedRequests()).isEqualTo(5);
        verify(eventRepository).save(e);
    }

    @Test
    void decrementConfirmedRequests_shouldDecreaseValue() {
        Event e = new Event();
        e.setConfirmedRequests(2);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));

        eventService.decrementConfirmedRequests(1L);

        assertThat(e.getConfirmedRequests()).isEqualTo(1);
        verify(eventRepository).save(e);
    }

    @Test
    void getEventsByParams_shouldReturnMappedDtos() {
        AdminEventSearchRequest request = new AdminEventSearchRequest();
        request.setFrom(0);
        request.setSize(10);

        Event e = new Event();
        EventFullDto dto = new EventFullDto();

        when(eventRepository.findByAdminFilter(any()))
                .thenReturn(List.of(e));
        when(eventMapper.toEventFullDto(e)).thenReturn(dto);

        List<EventFullDto> result = eventService.getEventsByParams(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    void getPublicEvents_shouldReturnMappedShortDtos() {
        PublicEventSearchRequest r = new PublicEventSearchRequest();
        r.setFrom(0);
        r.setSize(10);
        r.setSort("EVENT_DATE");

        Event e = new Event();
        EventShortDto dto = new EventShortDto();

//        when(eventRepository.findPublicEvents(any(), any(), any(), any(), any(), anyBoolean(), any()))
//                .thenReturn(new PageImpl<>(List.of(e)));
        when(eventRepository.findPublicEventsByFilter(any())).thenReturn(List.of(e));

        when(eventMapper.toEventShortDto(e)).thenReturn(dto);

        List<EventShortDto> result = eventService.getPublicEvents(r, request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    void getPublishedEventById_shouldFetchStatsAndMap() {
        Event e = new Event();
        e.setId(1L);
        e.setState(EventState.PUBLISHED);
        e.setPublishedOn(LocalDateTime.now().minusHours(1));

        ViewStatsDto view = new ViewStatsDto("/events/1", "app", 5L);

        when(eventRepository.findByIdAndState(eq(1L), eq(EventState.PUBLISHED))).thenReturn(Optional.of(e));
        when(statsClient.getStats(any(), any(), any(), eq(true))).thenReturn(List.of(view));
        when(eventMapper.toEventFullDto(e)).thenReturn(new EventFullDto());

        EventFullDto result = eventService.getPublishedEventById(1L, request);

        assertThat(result).isNotNull();
        assertThat(e.getViews()).isEqualTo(5);
    }
}