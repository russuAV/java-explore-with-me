package ru.practicum.category.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.NewCategoryDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

public class CategoryServiceImplITTest {

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private Category savedCategory;
    private User eventOwner;

    @BeforeEach
    void setUp() {
        // Очистка всех таблиц перед каждым тестом
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Настройка тестовых данных
        savedCategory = categoryRepository.save(new Category(null, "Концерты"));
        eventOwner = userRepository.save(new User(null, "user@example.com", "User Name"));
    }

    @Test
    void create_shouldSaveCategoryWithUniqueName() {
        NewCategoryDto dto = new NewCategoryDto("Кино");

        CategoryDto result = categoryService.create(dto);

        assertNotNull(result.getId());
        assertEquals("Кино", result.getName());
        assertEquals(2, categoryRepository.count()); // Изначально 1, + новая
    }

    @Test
    void delete_shouldRemoveCategoryWithoutEvents() {
        categoryService.delete(savedCategory.getId());

        assertEquals(0, categoryRepository.count());
        assertFalse(categoryRepository.existsById(savedCategory.getId()));
    }

    @Test
    void update_shouldChangeNameAndSaveInDb() {
        NewCategoryDto dto = new NewCategoryDto("Кино и театр");

        CategoryDto result = categoryService.update(savedCategory.getId(), dto);

        // Проверяем возвращенный DTO
        assertEquals("Кино и театр", result.getName());

        // Проверяем фактическое состояние в БД
        Category updated = categoryRepository.findById(savedCategory.getId()).get();
        assertEquals("Кино и театр", updated.getName());
    }

    @Test
    void create_shouldThrowConflictForDuplicateName() {
        NewCategoryDto dto = new NewCategoryDto("Концерты"); // Такое имя уже есть

        assertThrows(ConflictException.class, () -> categoryService.create(dto));
        assertEquals(1, categoryRepository.count()); // Новая запись не добавилась
    }

    @Test
    void delete_shouldThrowConflictWhenCategoryHasEvents() {
        // Создаем связанное событие
        eventRepository.save(Event.builder()
                .title("Концерт")
                .annotation("Explicabo animi exercitationem. Temporibus quae eum. Deserunt aperiam quod facilis " +
                        "voluptates fugit rerum rem assumenda. Non culpa earum saepe impedit necessitatibus nesciunt")
                .description("Odio sint delectus beatae nulla perferendis voluptas velit sunt.")
                .createdOn(LocalDateTime.now())
                .category(savedCategory)
                .initiator(eventOwner)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new Location(1.0, 1.0))
                .paid(true)
                .state(EventState.PENDING)
                .build());

        assertThrows(ConflictException.class,
                () -> categoryService.delete(savedCategory.getId()));

        assertTrue(categoryRepository.existsById(savedCategory.getId()));
    }

    @Test
    void getEntityById_shouldReturnExistingCategory() {
        Category result = categoryService.getEntityById(savedCategory.getId());

        assertEquals(savedCategory.getId(), result.getId());
        assertEquals("Концерты", result.getName());
    }

    @Test
    void getCategories_shouldReturnPaginatedResults() {
        // Добавляем еще категории
        categoryRepository.saveAll(List.of(
                new Category(null, "Кино"),
                new Category(null, "Театр"),
                new Category(null, "Выставки")
        ));

        // Запрашиваем страницу 1 (размер 2) - элементы 2 и 3
        List<CategoryDto> result = categoryService.getCategories(2, 2);

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .anyMatch(dto -> dto.getName().equals("Театр") ||
                        dto.getName().equals("Выставки")));
    }

    @Test
    void getCategories_whenNoCategories_shouldReturnEmptyList() {
        categoryRepository.deleteAll(); // Очищаем тестовые данные

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertTrue(result.isEmpty());
    }

}
