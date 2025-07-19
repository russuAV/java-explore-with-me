package ru.practicum.user.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.user.model.NewUserRequest;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.model.UserShortDto;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toUser_shouldMapCorrectly() {
        NewUserRequest request = NewUserRequest.builder()
                .email("user@example.com")
                .name("John Doe")
                .build();

        User user = mapper.toUser(request);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(request.getEmail());
        assertThat(user.getName()).isEqualTo(request.getName());
    }

    @Test
    void toUserDto_shouldMapCorrectly() {
        User user = new User(1L, "user@example.com", "Jane Doe");

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("user@example.com");
        assertThat(dto.getName()).isEqualTo("Jane Doe");
    }

    @Test
    void toUserShortDto_shouldMapCorrectly() {
        User user = new User(42L, "user42@example.com", "Shorty");

        UserShortDto dto = mapper.toUserShortDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getName()).isEqualTo("Shorty");
    }
}