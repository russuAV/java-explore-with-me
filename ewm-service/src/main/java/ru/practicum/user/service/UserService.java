package ru.practicum.user.service;

import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest newUserRequest);

    UserDto getUserById(Long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void delete(Long userId);

    User getEntityById(Long id);
}