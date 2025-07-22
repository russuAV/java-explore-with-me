package ru.practicum.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByIdIn_shouldReturnUsers() {
        User user1 = userRepository.save(new User(null, "first@mail.com", "First"));
        User user2 = userRepository.save(new User(null, "second@mail.com", "Second"));

        List<User> found = userRepository.findByIdIn(
                List.of(user1.getId(), user2.getId()),
                PageRequest.of(0, 10)
        );

        assertThat(found).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void existsById_shouldReturnTrue() {
        User user = userRepository.save(new User(null, "user@mail.com", "User"));

        boolean exists = userRepository.existsById(user.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        userRepository.save(new User(null, "taken@mail.com", "Taken"));

        boolean exists = userRepository.existsByEmail("taken@mail.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("free@mail.com");

        assertThat(exists).isFalse();
    }
}