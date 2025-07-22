package ru.practicum.event.participation.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.participation.model.EventRequestStatusUpdateRequest;
import ru.practicum.event.participation.model.ParticipationRequestDto;
import ru.practicum.event.participation.model.RequestState;
import ru.practicum.event.participation.repository.ParticipationRequestRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class ParticipationRequestServiceIT {

    @Autowired private ParticipationRequestService requestService;
    @Autowired private UserService userService;
    @Autowired private EventService eventService;
    @Autowired private ParticipationRequestRepository requestRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager em;
    @Autowired private CategoryRepository categoryRepository;

    private User initiator;
    private User user;
    private Event event;
    private Category category;

    @BeforeEach
    void setup() {
        initiator = userRepository.save(new User(null, "organizer@mail.com", "Организатор"));
        user = userRepository.save(new User(null, "guest@mail.com", "Гость"));
        category = categoryRepository.save(new Category(1L, "Movie"));

        event = Event.builder()
                .title("Событие")
                .annotation("Odio sint delectus beatae nulla")
                .description("Odio sint delectus beatae nulla")
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .initiator(initiator)
                .category(category)
                .state(EventState.PUBLISHED)
                .location(new Location(1.0, 1.0))
                .createdOn(LocalDateTime.now())
                .build();

        event = eventRepository.save(event);
    }

    @Test
    void createRequest_shouldWork() {
        ParticipationRequestDto dto = requestService.createRequest(user.getId(), event.getId());

        assertThat(dto.getRequester()).isEqualTo(user.getId());
        assertThat(dto.getEvent()).isEqualTo(event.getId());
        assertThat(dto.getStatus()).isEqualTo(RequestState.PENDING);
    }

    @Test
    void createRequest_shouldThrowConflict_whenUserIsInitiator() {
        assertThatThrownBy(() -> requestService.createRequest(initiator.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Инициатор события не может добавить запрос");
    }

    @Test
    void createRequest_shouldThrowConflict_whenParticipantLimitReached() {
        event.setParticipantLimit(1);
        event.setConfirmedRequests(1);
        em.merge(event);

        assertThatThrownBy(() -> requestService.createRequest(user.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Лимит участников события достигнут");
    }

    @Test
    void createRequest_shouldThrowConflict_whenEventNotPublished() {
        event.setState(EventState.PENDING);
        em.merge(event);

        assertThatThrownBy(() -> requestService.createRequest(user.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Нельзя участвовать в неопубликованном событии");
    }

    @Test
    void createRequest_shouldAutoConfirm_whenLimitIsZero() {
        event.setParticipantLimit(0);
        em.merge(event);

        ParticipationRequestDto dto = requestService.createRequest(user.getId(), event.getId());

        assertThat(dto.getStatus()).isEqualTo(RequestState.CONFIRMED);
    }

    @Test
    void createRequest_shouldAutoConfirm_whenModerationOff() {
        event.setRequestModeration(false);
        event.setParticipantLimit(10);
        em.merge(event);

        ParticipationRequestDto dto = requestService.createRequest(user.getId(), event.getId());

        assertThat(dto.getStatus()).isEqualTo(RequestState.CONFIRMED);
    }

    @Test
    void cancelRequest_shouldWork_whenConfirmedRequest() {
        event.setParticipantLimit(1);
        event.setRequestModeration(false);
        em.merge(event);

        ParticipationRequestDto confirmed = requestService.createRequest(user.getId(), event.getId());

        ParticipationRequestDto canceled = requestService.cancelRequest(user.getId(), confirmed.getId());

        assertThat(canceled.getStatus()).isEqualTo(RequestState.CANCELED);
    }

    @Test
    void cancelRequest_shouldWork_whenPendingRequest() {
        ParticipationRequestDto dto = requestService.createRequest(user.getId(), event.getId());

        ParticipationRequestDto canceled = requestService.cancelRequest(user.getId(), dto.getId());

        assertThat(canceled.getStatus()).isEqualTo(RequestState.CANCELED);
    }

    @Test
    void cancelRequest_shouldThrowForbidden_whenNotOwner() {
        ParticipationRequestDto dto = requestService.createRequest(user.getId(), event.getId());

        assertThatThrownBy(() -> requestService.cancelRequest(initiator.getId(), dto.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("может отменить только свою заявку");
    }

    @Test
    void cancelRequest_shouldThrowNotFound_whenRequestNotExists() {
        assertThatThrownBy(() -> requestService.cancelRequest(user.getId(), 9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void getOwnRequests_shouldReturnCreatedRequest() {
        requestService.createRequest(user.getId(), event.getId());

        List<ParticipationRequestDto> requests = requestService.getOwnRequests(user.getId());

        assertThat(requests).hasSize(1);
    }

    @Test
    void getParticipationRequestsOwnEvents_shouldReturnRequest() {
        requestService.createRequest(user.getId(), event.getId());

        List<ParticipationRequestDto> requests = requestService.getParticipationRequestsOwnEvents(
                initiator.getId(), event.getId());

        assertThat(requests).hasSize(1);
    }

    @Test
    void getParticipationRequestsOwnEvents_shouldThrowForbidden_whenNotInitiator() {
        requestService.createRequest(user.getId(), event.getId());

        assertThatThrownBy(() ->
                requestService.getParticipationRequestsOwnEvents(user.getId(), event.getId())
        ).isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("может увидеть запросы");
    }

    @Test
    void getParticipationRequestsOwnEvents_shouldReturnEmpty_whenNoRequests() {
        List<ParticipationRequestDto> requests = requestService.getParticipationRequestsOwnEvents(
                initiator.getId(), event.getId());

        assertThat(requests).isEmpty();
    }

    @Test
    void updateStatusForRequests_shouldConfirmAndRejectProperly() {
        User anotherUser = userRepository.save(new User(null, "another@mail.com", "Второй"));
        ParticipationRequestDto r1 = requestService.createRequest(user.getId(), event.getId());
        ParticipationRequestDto r2 = requestService.createRequest(anotherUser.getId(), event.getId());

        // Ставим лимит 1
        event.setParticipantLimit(1);
        em.merge(event);

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest(
                List.of(r1.getId(), r2.getId()), RequestState.CONFIRMED
        );

        var result = requestService.updateStatusForRequests(initiator.getId(), event.getId(), request);

        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests()).hasSize(1);
    }

    @Test
    void updateStatus_shouldThrowForbidden_whenUserNotInitiator() {
        User notInitiator = userRepository.save(new User(null, "fake@mail.com", "НеОрганизатор"));

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest(
                List.of(), RequestState.CONFIRMED
        );

        assertThatThrownBy(() -> requestService.updateStatusForRequests(
                notInitiator.getId(), event.getId(), request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Только инициатор события может изменить статус запроса на участие");
    }

    @Test
    void updateStatus_shouldThrowConflict_whenLimitReached() {
        event.setParticipantLimit(0);
        em.merge(event);

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest(
                List.of(), RequestState.CONFIRMED
        );

        assertThatThrownBy(() -> requestService.updateStatusForRequests(
                initiator.getId(), event.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Нет свободных мест для участия в данном событии");
    }

    @Test
    void updateStatus_shouldThrowConflict_whenModerationOff() {
        event.setRequestModeration(false);
        event.setParticipantLimit(10);
        em.merge(event);

        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest(
                List.of(), RequestState.CONFIRMED
        );

        assertThatThrownBy(() -> requestService.updateStatusForRequests(
                initiator.getId(), event.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Подтверждение заявок не требуется");
    }

    @Test
    void updateStatus_shouldThrowNotFound_whenRequestsNotExist() {
        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest(
                List.of(999L), RequestState.CONFIRMED
        );

        assertThatThrownBy(() -> requestService.updateStatusForRequests(
                initiator.getId(), event.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдены");
    }

    @Test
    void updateStatus_shouldThrowConflict_whenNotPending() {
        ParticipationRequestDto created = requestService.createRequest(user.getId(), event.getId());

        // Принудительно переводим заявку в CONFIRMED
        var request = requestRepository.findById(created.getId()).get();
        request.setStatus(RequestState.CONFIRMED);
        requestRepository.save(request);

        EventRequestStatusUpdateRequest requestUpdate = new EventRequestStatusUpdateRequest(
                List.of(created.getId()), RequestState.CONFIRMED
        );

        assertThatThrownBy(() -> requestService.updateStatusForRequests(
                initiator.getId(), event.getId(), requestUpdate))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("только заявки со статусом PENDING");
    }
}