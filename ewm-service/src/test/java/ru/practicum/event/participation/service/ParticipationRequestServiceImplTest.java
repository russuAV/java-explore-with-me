package ru.practicum.event.participation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.participation.mapper.ParticipationRequestMapper;
import ru.practicum.event.participation.model.*;
import ru.practicum.event.participation.repository.ParticipationRequestRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipationRequestServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private EventService eventService;
    @Mock
    private ParticipationRequestRepository requestRepository;
    @Mock
    private ParticipationRequestMapper requestMapper;

    @InjectMocks
    private ParticipationRequestServiceImpl service;

    private User user;
    private Event event;
    private ParticipationRequest request;

    @BeforeEach
    void setup() {
        user = new User(1L, "user@mail.com", "User");
        event = Event.builder()
                .id(100L)
                .initiator(new User(2L, "init@mail.com", "Init"))
                .participantLimit(10)
                .confirmedRequests(5)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title("Sample")
                .build();
        request = ParticipationRequest.builder()
                .id(55L)
                .event(event)
                .requester(user)
                .status(RequestState.PENDING)
                .build();
    }

    @Test
    void getOwnRequests_returnsList() {
        when(userService.getEntityById(1L)).thenReturn(user);
        when(requestRepository.findAllByRequesterId(1L)).thenReturn(List.of(request));
        ParticipationRequestDto dto = new ParticipationRequestDto();
        when(requestMapper.toDto(any())).thenReturn(dto);

        List<ParticipationRequestDto> result = service.getOwnRequests(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void getParticipationRequestsOwnEvents_whenNotInitiator_thenForbidden() {
        event.setInitiator(user);
        when(userService.getEntityById(1L)).thenReturn(user);
        when(eventService.getEntityById(100L)).thenReturn(event);
        event.setInitiator(new User(9L, "other@mail.com", "Other"));

        assertThatThrownBy(() -> service.getParticipationRequestsOwnEvents(1L, 100L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateStatusForRequests_confirmedSuccess() {
        event.setInitiator(user);
        event.setConfirmedRequests(0);
        event.setParticipantLimit(5);
        event.setRequestModeration(true);

        request.setStatus(RequestState.PENDING);
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest(List.of(55L), RequestState.CONFIRMED);

        when(userService.getEntityById(1L)).thenReturn(user);
        when(eventService.getEntityById(100L)).thenReturn(event);
        when(requestRepository.findAllByIdIn(List.of(55L))).thenReturn(List.of(request));
        when(requestMapper.toDto(any())).thenReturn(new ParticipationRequestDto());

        EventRequestStatusUpdateResult result = service.updateStatusForRequests(1L, 100L, updateRequest);
        assertThat(result.getConfirmedRequests()).hasSize(1);
    }

    @Test
    void updateStatusForRequests_whenNotPending_thenThrowConflict() {
        event.setInitiator(user);
        event.setParticipantLimit(5);
        request.setStatus(RequestState.CONFIRMED);
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest(List.of(55L), RequestState.CONFIRMED);

        when(userService.getEntityById(1L)).thenReturn(user);
        when(eventService.getEntityById(100L)).thenReturn(event);

        assertThatThrownBy(() -> service.updateStatusForRequests(1L, 100L, updateRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateStatusForRequests_whenEmptyRequests_thenNotFound() {
        event.setInitiator(user);
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest(List.of(777L), RequestState.CONFIRMED);

        when(userService.getEntityById(1L)).thenReturn(user);
        when(eventService.getEntityById(100L)).thenReturn(event);
        when(requestRepository.findAllByIdIn(List.of(777L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.updateStatusForRequests(1L, 100L, updateRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatusForRequests_whenRejected_thenAllMarkedRejected() {
        event.setInitiator(user);
        event.setRequestModeration(true);
        event.setParticipantLimit(2);
        event.setConfirmedRequests(0);
        request.setStatus(RequestState.PENDING);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest(List.of(55L), RequestState.REJECTED);

        when(userService.getEntityById(1L)).thenReturn(user);
        when(eventService.getEntityById(100L)).thenReturn(event);
        when(requestRepository.findAllByIdIn(List.of(55L))).thenReturn(List.of(request));
        when(requestMapper.toDto(any())).thenReturn(new ParticipationRequestDto());

        EventRequestStatusUpdateResult result = service.updateStatusForRequests(1L, 100L, updateRequest);
        assertThat(result.getRejectedRequests()).hasSize(1);
    }
}
