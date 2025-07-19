package ru.practicum.event.participation.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.category.model.Category;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.Event;
import ru.practicum.event.participation.model.ParticipationRequest;
import ru.practicum.event.participation.model.RequestState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ParticipationRequestRepositoryTest {

    @Autowired
    private ParticipationRequestRepository repository;

    @Autowired
    private TestEntityManager em;

    private User initiator;
    private User requester;
    private Category category;

    @BeforeEach
    void setUp() {
        initiator = em.persist(new User(null, "init@mail.com", "Initiator"));
        requester = em.persist(new User(null, "req@mail.com", "Requester"));
        category = em.persist(new Category(null, "Movie"));
    }

    private Event createEvent(User user, String title, double lat, double lon) {
        return em.persist(Event.builder()
                .title(title)
                .annotation("TestTestTestTestTest")
                .description("DescDescDescDescDesc")
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .initiator(user)
                .category(category)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .location(new Location(lat, lon))
                .state(ru.practicum.event.model.state.EventState.PENDING)
                .build());
    }

    @Test
    void existsByEventIdAndRequesterId_shouldReturnTrue() {
        Event event = createEvent(initiator, "Event1", 0.0, 0.0);
        ParticipationRequest request = em.persist(ParticipationRequest.builder()
                .event(event)
                .requester(initiator)
                .created(LocalDateTime.now())
                .status(RequestState.PENDING)
                .build());

        boolean exists = repository.existsByEventIdAndRequesterId(event.getId(), initiator.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void findAllByRequesterId_shouldReturnRequests() {
        Event event = createEvent(requester, "Event2", 1.0, 1.0);

        ParticipationRequest req = em.persist(ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(RequestState.CONFIRMED)
                .build());

        List<ParticipationRequest> list = repository.findAllByRequesterId(requester.getId());
        assertThat(list).containsExactly(req);
    }

    @Test
    void findAllByEventIdAndEventInitiatorId_shouldReturnCorrectRequests() {
        Event event = createEvent(initiator, "Event3", 2.0, 2.0);

        ParticipationRequest req = em.persist(ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(RequestState.PENDING)
                .build());

        List<ParticipationRequest> found =
                repository.findAllByEventIdAndEvent_Initiator_Id(event.getId(), initiator.getId());
        assertThat(found).containsExactly(req);
    }

    @Test
    void findAllByIdIn_shouldReturnMatchingRequests() {
        Event event = createEvent(initiator, "Event4", 3.0, 3.0);

        ParticipationRequest req1 = em.persist(ParticipationRequest.builder()
                .event(event)
                .requester(initiator)
                .created(LocalDateTime.now())
                .status(RequestState.CONFIRMED)
                .build());

        ParticipationRequest req2 = em.persist(ParticipationRequest.builder()
                .event(event)
                .requester(initiator)
                .created(LocalDateTime.now())
                .status(RequestState.REJECTED)
                .build());

        List<ParticipationRequest> list = repository.findAllByIdIn(List.of(req1.getId(), req2.getId()));
        assertThat(list).containsExactlyInAnyOrder(req1, req2);
    }
}