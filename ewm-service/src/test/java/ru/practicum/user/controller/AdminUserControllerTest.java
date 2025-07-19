package ru.practicum.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        NewUserRequest request = new NewUserRequest("user@mail.com", "User");
        UserDto response = new UserDto("user@mail.com", 1L,  "User");

        Mockito.when(userService.create(request)).thenReturn(response);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("user@mail.com")))
                .andExpect(jsonPath("$.name", is("User")));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserDto userDto = new UserDto("user@mail.com", 1L, "User");
        Mockito.when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("user@mail.com")))
                .andExpect(jsonPath("$.name", is("User")));
    }

    @Test
    void getUsers_shouldReturnUserList() throws Exception {
        List<UserDto> users = List.of(
                new UserDto("one@mail.com", 1L, "One"),
                new UserDto("two@mail.com", 2L,  "Two")
        );
        Mockito.when(userService.getUsers(null, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).delete(1L);
    }

    @Test
    void whenEmailIsInvalid_thenReturns400() throws Exception {
        NewUserRequest invalidEmail = NewUserRequest.builder()
                .email("invalid-email")
                .name("Valid Name")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenEmailIsBlank_thenReturns400() throws Exception {
        NewUserRequest blankEmail = NewUserRequest.builder()
                .email(" ")
                .name("Valid Name")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenNameTooShort_thenReturns400() throws Exception {
        NewUserRequest shortName = NewUserRequest.builder()
                .email("user@mail.com")
                .name("A")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenNameIsBlank_thenReturns400() throws Exception {
        NewUserRequest blankName = NewUserRequest.builder()
                .email("user@mail.com")
                .name("  ")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankName)))
                .andExpect(status().isBadRequest());
    }
}