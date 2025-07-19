package ru.practicum.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.model.AdminEventSearchRequest;
import ru.practicum.event.model.EventFullDto;
import ru.practicum.event.model.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminEventController.class)
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getEventsByParams_shouldReturnList() throws Exception {
        EventFullDto dto = EventFullDto.builder()
                .id(1L)
                .title("Event title")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(eventService.getEventsByParams(any(AdminEventSearchRequest.class)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void updateEvent_shouldReturnUpdatedDto() throws Exception {
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setTitle("Updated title");

        EventFullDto updated = EventFullDto.builder()
                .id(1L)
                .title("Updated title")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(5))
                .build();

        Mockito.when(eventService.updateByAdmin(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));
    }

    @Test
    void updateEvent_shouldFailValidation_whenFieldsInvalid() throws Exception {
        UpdateEventAdminRequest invalidRequest = new UpdateEventAdminRequest();
        invalidRequest.setTitle("ab"); // слишком короткий
        invalidRequest.setAnnotation("short"); // слишком короткий
        invalidRequest.setDescription("x".repeat(8000)); // слишком длинный

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}