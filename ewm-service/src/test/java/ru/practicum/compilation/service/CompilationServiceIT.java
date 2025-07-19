package ru.practicum.compilation.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
class CompilationServiceIT {

    @Autowired
    private CompilationService compilationService;

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User savedUser;
    private Category savedCategory;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(new User(null, "test@mail.com", "Test User"));
        savedCategory = categoryRepository.save(new Category(null, "Концерты"));
    }

    private Event createValidEvent(String title) {
        Event event = Event.builder()
                .title(title)
                .annotation("Explicabo animi exercitationem. Temporibus quae eum")
                .description("Odio sint delectus beatae nulla perferendis voluptas velit sunt.")
                .createdOn(LocalDateTime.now().minusDays(1))
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(false)
                .state(EventState.PENDING)
                .location(new Location(55.0, 37.0))
                .category(savedCategory)
                .initiator(savedUser)
                .build();
        return entityManager.merge(event);
    }

    @Test
    void create_shouldPersistCompilationAndReturnDto() {
        Event event = createValidEvent("Concert");

        NewCompilationDto dto = new NewCompilationDto(Set.of(event.getId()), true, "Top events");

        CompilationDto result = compilationService.create(dto);

        assertThat(result.getTitle()).isEqualTo("Top events");
        assertThat(result.getEvents()).hasSize(1);
    }

    @Test
    void create_shouldCreateEmptyCompilation_whenNoEventsProvided() {
        NewCompilationDto dto = new NewCompilationDto(null, false, "Empty compilation");

        CompilationDto result = compilationService.create(dto);

        assertThat(result.getTitle()).isEqualTo("Empty compilation");
        assertThat(result.getEvents()).isEmpty();
    }

    @Test
    void update_shouldModifyCompilation() {
        Event event = createValidEvent("Old");

        Compilation compilation = new Compilation();
        compilation.setTitle("Old compilation");
        compilation.setPinned(false);
        compilation.setEvents(Set.of(event));
        compilation = compilationRepository.save(compilation);

        Event newEvent = createValidEvent("New");

        UpdateCompilationRequest update = new UpdateCompilationRequest(
                Set.of(newEvent.getId()), true, "Updated");

        CompilationDto updated = compilationService.update(compilation.getId(), update);

        assertThat(updated.getTitle()).isEqualTo("Updated");
        assertThat(updated.getPinned()).isTrue();
        assertThat(updated.getEvents())
                .extracting(e -> e.getId())
                .containsExactly(newEvent.getId());
    }

    @Test
    void update_shouldRetainOldEvents_whenNoNewEventsProvided() {
        Event oldEvent = createValidEvent("Old event");

        Compilation compilation = new Compilation();
        compilation.setTitle("Initial");
        compilation.setPinned(false);
        compilation.setEvents(new HashSet<>(Set.of(oldEvent)));
        compilation = compilationRepository.save(compilation);

        // Обновляем только pinned и title, events = null
        UpdateCompilationRequest update = new UpdateCompilationRequest(
                null,
                true,
                "Updated title"
        );

        CompilationDto updated = compilationService.update(compilation.getId(), update);

        assertThat(updated.getTitle()).isEqualTo("Updated title");
        assertThat(updated.getPinned()).isTrue();
        assertThat(updated.getEvents())
                .extracting(e -> e.getId())
                .containsExactly(oldEvent.getId()); // должен остаться старый event
    }

    @Test
    void delete_shouldRemoveCompilation() {
        Compilation comp = new Compilation();
        comp.setTitle("To delete");
        comp = compilationRepository.save(comp);

        compilationService.delete(comp.getId());

        assertThat(compilationRepository.findById(comp.getId())).isEmpty();
    }

    @Test
    void getCompilationById_shouldReturnEnrichedDto() {
        Event e = createValidEvent("Test");

        Compilation c = new Compilation();
        c.setTitle("Comp");
        c.setPinned(true);
        c.setEvents(Set.of(e));
        c = compilationRepository.save(c);

        CompilationDto dto = compilationService.getCompilationById(c.getId(), null);

        assertThat(dto.getTitle()).isEqualTo("Comp");
        assertThat(dto.getEvents()).hasSize(1);
    }

    @Test
    void getAllCompilations_shouldReturnFiltered() {
        Compilation pinned = new Compilation();
        pinned.setTitle("Pinned");
        pinned.setPinned(true);
        compilationRepository.save(pinned);

        Compilation notPinned = new Compilation();
        notPinned.setTitle("Not pinned");
        notPinned.setPinned(false);
        compilationRepository.save(notPinned);

        List<CompilationDto> pinnedList = compilationService.getAllCompilations(
                true, 0, 10, null);
        assertThat(pinnedList).hasSize(1);
        assertThat(pinnedList.get(0).getTitle()).isEqualTo("Pinned");

        List<CompilationDto> all = compilationService.getAllCompilations(
                null, 0, 10, null);
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }
}