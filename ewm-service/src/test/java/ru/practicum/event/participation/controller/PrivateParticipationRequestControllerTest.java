package ru.practicum.event.participation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.participation.model.ParticipationRequestDto;
import ru.practicum.event.participation.model.RequestState;
import ru.practicum.event.participation.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateParticipationRequestController.class)
class PrivateParticipationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipationRequestService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRequest_shouldReturnCreated() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(
                LocalDateTime.now(), 2L, 1L, 1L,RequestState.PENDING);

        Mockito.when(service.createRequest(1L, 2L)).thenReturn(dto);

        mockMvc.perform(post("/users/1/requests?eventId=2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.event").value(2L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void cancelRequest_shouldReturnUpdatedRequest() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(
                LocalDateTime.now(), 2L, 1L, 1L, RequestState.CANCELED);

        Mockito.when(service.cancelRequest(1L, 1L)).thenReturn(dto);

        mockMvc.perform(patch("/users/1/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void getOwnRequests_shouldReturnList() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(
                LocalDateTime.now(), 2L, 1L, 1L,RequestState.PENDING);

        Mockito.when(service.getOwnRequests(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}