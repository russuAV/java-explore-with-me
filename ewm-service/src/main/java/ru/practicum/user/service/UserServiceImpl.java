package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Пользователь с email='" + newUserRequest.getEmail() + "' уже имеется в базе.");
        }
        User user = userMapper.toUser(newUserRequest);
        User createdUser = userRepository.save(user);

        log.info("Пользователь успешно создан с id: {}, email: {}",
                user.getId(), user.getEmail());
        return userMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = getEntityById(userId);
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (from < 0) throw new IllegalArgumentException("From parameter cannot be negative");
        if (size <= 0) throw new IllegalArgumentException("Size parameter must be positive");

        List<User> users = userRepository.findUsersWithOffset(ids, from, size);

        return users.stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        getEntityById(userId);
        userRepository.deleteById(userId);

        log.info("Пользователь с id {} удалён", userId);
    }

    @Override
    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + id + " не найден"));
    }
}