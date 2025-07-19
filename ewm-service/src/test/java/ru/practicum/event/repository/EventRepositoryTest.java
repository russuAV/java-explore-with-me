package ru.practicum.event.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Event prepareEvent(User user, Category category, EventState state, boolean paid, LocalDateTime eventDate) {
        return Event.builder()
                .title("Test")
                .annotation("Odio sint delectus beatae nulla")
                .description("Odio sint delectus beatae nulla")
                .category(category)
                .initiator(user)
                .eventDate(eventDate)
                .createdOn(LocalDateTime.now())
                .state(state)
                .paid(paid)
                .participantLimit(10)
                .confirmedRequests(0)
                .location(new ru.practicum.event.location.Location(0.0, 0.0))
                .build();
    }

    @Test
    void testFindAllByInitiatorId() {
        User user = userRepository.save(new User(null, "u@test.com", "User"));
        Category cat = categoryRepository.save(new Category(null, "Music"));

        Event e = prepareEvent(user, cat, EventState.PENDING, false, LocalDateTime.now().plusDays(1));
        eventRepository.save(e);

        Page<Event> page = eventRepository.findAllByInitiatorId(
                user.getId(), PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void testExistsByCategoryId() {
        User user = userRepository.save(new User(null, "a@a.com", "A"));
        Category cat = categoryRepository.save(new Category(null, "Sport"));
        eventRepository.save(prepareEvent(user, cat, EventState.PENDING, true, LocalDateTime.now().plusDays(1)));

        assertThat(eventRepository.existsByCategoryId(cat.getId())).isTrue();
    }

    @Test
    void testFindByAdminParams() {
        User user = userRepository.save(new User(null, "admin@site.com", "Admin"));
        Category cat = categoryRepository.save(new Category(null, "Movie"));

        Event e = prepareEvent(user, cat, EventState.PUBLISHED, true, LocalDateTime.now().plusDays(3));
        eventRepository.save(e);

        Page<Event> result = eventRepository.findByAdminParams(
                List.of(user.getId()),
                List.of(EventState.PUBLISHED),
                List.of(cat.getId()),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10),
                PageRequest.of(0, 5)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCategory().getName()).isEqualTo("Movie");
    }

    @Test
    void testFindPublicEvents() {
        User user = userRepository.save(new User(null, "pub@lic.com", "Public"));
        Category cat = categoryRepository.save(new Category(null, "Theater"));

        Event e = prepareEvent(user, cat, EventState.PUBLISHED, true, LocalDateTime.now().plusDays(5));
        eventRepository.save(e);

        Page<Event> result = eventRepository.findPublicEvents(
                "Odio", List.of(cat.getId()), true,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10),
                false,
                PageRequest.of(0, 5)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testFindByIdAndState() {
        User user = userRepository.save(new User(null, "state@t.com", "S"));
        Category cat = categoryRepository.save(new Category(null, "Comedy"));

        Event e = prepareEvent(user, cat, EventState.PUBLISHED, false, LocalDateTime.now().plusDays(1));
        eventRepository.save(e);

        Optional<Event> found = eventRepository.findByIdAndState(e.getId(), EventState.PUBLISHED);
        assertThat(found).isPresent();
    }

    @Test
    void testFindByIdIn() {
        User user = userRepository.save(new User(null, "multi@id.com", "M"));
        Category cat = categoryRepository.save(new Category(null, "Workshop"));

        Event e1 = eventRepository.save(prepareEvent(
                user, cat, EventState.PENDING, true, LocalDateTime.now().plusDays(2)));
        Event e2 = eventRepository.save(prepareEvent(
                user, cat, EventState.PENDING, false, LocalDateTime.now().plusDays(3)));

        Set<Event> result = eventRepository.findByIdIn(Set.of(e1.getId(), e2.getId()));
        assertThat(result).hasSize(2);
    }
}
