package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest newUserRequest) {
        return userService.create(newUserRequest);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long userId) {
        userService.delete(userId);
    }
}