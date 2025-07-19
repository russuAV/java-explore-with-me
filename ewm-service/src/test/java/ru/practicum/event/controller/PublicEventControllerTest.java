package ru.practicum.event.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.StatsClient;
import ru.practicum.event.model.EventFullDto;
import ru.practicum.event.model.EventShortDto;
import ru.practicum.event.model.PublicEventSearchRequest;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicEventController.class)
class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private StatsClient statsClient;

    @Test
    void getPublicEvents_shouldReturnList() throws Exception {
        EventShortDto dto = new EventShortDto();
        dto.setId(1L);
        dto.setTitle("Title");

        Mockito.when(eventService.getPublicEvents(any(PublicEventSearchRequest.class), any()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/events")
                        .param("text", "Title")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Title")));
    }

    @Test
    void getPublishedEventById_shouldReturnFullDto() throws Exception {
        EventFullDto dto = EventFullDto.builder()
                .id(1L)
                .title("Event")
                .annotation("Ann")
                .description("Desc")
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();

        Mockito.when(eventService.getPublishedEventById(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Event")));
    }

    @Test
    void getPublicEvents_shouldReturn400_whenInvalidParams() throws Exception {
        mockMvc.perform(get("/events")
                        .param("from", "-1") //  не может быть отрицательным
                        .param("size", "0")  //  должен быть положительным
                        .param("rangeStart", "2030-01-02 12:00:00") // не может быть после rangeEnd
                        .param("rangeEnd", "2030-01-01 12:00:00"))
                .andExpect(status().isBadRequest());
    }
}