package ru.practicum.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapperImpl;
import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserServiceImplIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, new UserMapperImpl());
    }

    @Test
    void create_shouldCreateUser() {
        NewUserRequest newUser = new NewUserRequest("test@example.com", "Test User");

        UserDto result = userService.create(newUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void create_shouldThrowConflictException_whenEmailExists() {
        em.persist(new User(null, "test@example.com", "User"));

        NewUserRequest newUser = new NewUserRequest("test@example.com", "New User");

        assertThatThrownBy(() -> userService.create(newUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже имеется");
    }

    @Test
    void getUserById_shouldReturnUser() {
        User saved = em.persist(new User(null, "mail@example.com", "User"));
        em.flush();

        UserDto dto = userService.getUserById(saved.getId());

        assertThat(dto.getEmail()).isEqualTo("mail@example.com");
    }

    @Test
    void getUserById_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUsers_shouldReturnAll_whenIdsNull() {
        User u1 = em.persist(new User(null, "a@mail.com", "A"));
        User u2 = em.persist(new User(null, "b@mail.com", "B"));

        List<UserDto> users = userService.getUsers(null, 0, 10);

        assertThat(users).hasSize(2);
    }

    @Test
    void getUsers_shouldReturnFiltered_whenIdsProvided() {
        User u1 = em.persist(new User(null, "a@mail.com", "A"));
        User u2 = em.persist(new User(null, "b@mail.com", "B"));

        List<UserDto> users = userService.getUsers(List.of(u2.getId()), 0, 10);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("b@mail.com");
    }


    @Test
    void delete_shouldRemoveUser() {
        User user = em.persist(new User(null, "del@mail.com", "To Delete"));

        userService.delete(user.getId());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    @Test
    void delete_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }
}