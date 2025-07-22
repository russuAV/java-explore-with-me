package ru.practicum.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldCreateUser() {
        NewUserRequest request = new NewUserRequest("test@example.com", "Test User");
        User user = new User(null, "test@example.com", "Test User");
        User saved = new User(1L, "test@example.com", "Test User");
        UserDto expectedDto = new UserDto("test@example.com", 1L, "Test User");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toUserDto(saved)).thenReturn(expectedDto);

        UserDto result = userService.create(request);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void create_shouldThrowConflictException_whenEmailExists() {
        NewUserRequest request = new NewUserRequest("duplicate@example.com", "Dup User");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже имеется");
    }

    @Test
    void getUserById_shouldReturnUserDto() {
        User user = new User(1L, "test@example.com", "Test User");
        UserDto dto = new UserDto("test@example.com", 1L, "Test User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(dto);

        UserDto result = userService.getUserById(1L);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getUserById_shouldThrowNotFound_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getUsers_shouldReturnAllUsers_whenIdsNull() {
        User user = new User(1L, "mail@example.com", "User");
        UserDto dto = new UserDto("mail@example.com", 1L, "User");
        int from = 0;
        int size = 10;

        when(userRepository.findUsersWithOffset(null, from, size)).thenReturn(List.of(user));
        when(userMapper.toUserDto(user)).thenReturn(dto);

        List<UserDto> result = userService.getUsers(null, from, size);

        assertThat(result).containsExactly(dto);
        verify(userRepository).findUsersWithOffset(null, from, size);
    }

    @Test
    void getUsers_shouldReturnFilteredUsers_whenIdsProvided() {
        List<Long> ids = List.of(1L, 2L);
        User user1 = new User(1L, "a@a.com", "A");
        UserDto dto1 = new UserDto("a@a.com", 1L, "A");
        int from = 0;
        int size = 10;

        when(userRepository.findUsersWithOffset(ids, from, size)).thenReturn(List.of(user1));
        when(userMapper.toUserDto(user1)).thenReturn(dto1);

        List<UserDto> result = userService.getUsers(ids, from, size);

        assertThat(result).containsExactly(dto1);
        verify(userRepository).findUsersWithOffset(ids, from, size);
    }

    @Test
    void delete_shouldRemoveUser() {
        User user = new User(1L, "test@example.com", "Test User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowNotFound_whenUserMissing() {
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(77L))
                .isInstanceOf(NotFoundException.class);
    }
}