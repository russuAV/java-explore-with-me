package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;

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
        Pageable pageable = PageRequest.of(from / size, size);

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findByIdIn(ids, pageable);
        }

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