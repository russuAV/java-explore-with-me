package ru.practicum.stats.hit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EndpointHitDto;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.LocalDateTime;

@WebMvcTest(HitController.class)
@ContextConfiguration(classes = {HitController.class, HitServiceImpl.class})

class HitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HitService hitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postHit_shouldReturn201AndCallService() throws Exception {
        EndpointHitDto dto = new EndpointHitDto(
                "main-service",
                "/events",
                "192.168.0.1",
                LocalDateTime.now()
        );

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(hitService, times(1)).save(any(EndpointHitDto.class));
    }

    @Test
    void saveHit_shouldReturn400WhenInvalid() throws Exception {
        String invalidJson = """
        "app": "",
        "uri": null,
        "ip": "invalid",
        "timestamp": "not-a-date"
    """;

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(emptyOrNullString())); // Проверяем пустой ответ
    }
}