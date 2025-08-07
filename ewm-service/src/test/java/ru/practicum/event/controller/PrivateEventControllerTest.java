package ru.practicum.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.EventFullDto;
import ru.practicum.event.model.EventShortDto;
import ru.practicum.event.model.NewEventDto;
import ru.practicum.event.model.UpdateEventUserRequest;
import ru.practicum.event.participation.model.EventRequestStatusUpdateRequest;
import ru.practicum.event.participation.model.EventRequestStatusUpdateResult;
import ru.practicum.event.participation.model.ParticipationRequestDto;
import ru.practicum.event.participation.model.RequestState;
import ru.practicum.event.participation.service.ParticipationRequestService;
import ru.practicum.event.service.EventService;
import ru.practicum.user.model.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateEventController.class)
class PrivateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private ParticipationRequestService participationRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedEvent() throws Exception {
        NewEventDto newEvent = new NewEventDto();
        newEvent.setTitle("Concert");
        newEvent.setAnnotation("Odio sint delectus beatae nulla perferendis voluptas velit sunt.");
        newEvent.setDescription("Explicabo animi exercitationem. Temporibus quae eum.");
        newEvent.setEventDate(LocalDateTime.now().plusDays(2));
        newEvent.setPaid(false);
        newEvent.setCategory(1L);
        newEvent.setParticipantLimit(100);
        newEvent.setLocation(new Location(1.0, 1.0));

        EventFullDto created = EventFullDto.builder()
                .id(1L)
                .title("Concert")
                .annotation("Odio sint delectus beatae nulla perferendis voluptas velit sunt.")
                .description("Explicabo animi exercitationem. Temporibus quae eum.")
                .eventDate(newEvent.getEventDate())
                .build();

        Mockito.when(eventService.create(eq(1L), any(NewEventDto.class))).thenReturn(created);

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Concert"));
    }

    @Test
    void create_shouldReturn400_whenFieldsInvalid() throws Exception {
        // Аннотация и описание слишком короткие, нет title и category, location = null
        NewEventDto invalidEvent = new NewEventDto();
        invalidEvent.setAnnotation("short");
        invalidEvent.setDescription("tiny");
        invalidEvent.setEventDate(LocalDateTime.now().plusDays(1));
        invalidEvent.setParticipantLimit(-1); // отрицательное значение
        // остальные обязательные поля не заданы (например, title, category, location)

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserEvents_shouldReturnList() throws Exception {
        EventShortDto event = new EventShortDto("Annotation", new CategoryDto(), 0,
                LocalDateTime.now().plusDays(1), 5L, new UserShortDto(1L, null),
                null, null,
                "Concert", 0, 0);
        Mockito.when(eventService.getUserEvents(1L, 0, 10)).thenReturn(List.of(event));

        mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(5L));
    }

    @Test
    void getEventById_shouldReturnFullDto() throws Exception {
        EventFullDto dto = EventFullDto.builder()
                .id(1L)
                .title("Concert")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(eventService.getEventById(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/users/1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void update_shouldReturnUpdatedEvent() throws Exception {
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        updateRequest.setTitle("Updated concert");

        EventFullDto updated = EventFullDto.builder()
                .id(1L)
                .title("Updated concert")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(eventService.updateByInitiator(eq(1L), eq(1L), any(UpdateEventUserRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated concert"));
    }

    @Test
    void update_shouldReturn400_whenFieldsInvalid() throws Exception {
        UpdateEventUserRequest invalidUpdate = new UpdateEventUserRequest();
        invalidUpdate.setAnnotation("short"); // < 20
        invalidUpdate.setDescription("tiny"); // < 20
        invalidUpdate.setTitle("no");         // < 3 символов
        invalidUpdate.setParticipantLimit(-5); // отрицательное число

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getParticipationRequestsOwnEvents_shouldReturnList() throws Exception {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .requester(2L)
                .event(1L)
                .build();

        Mockito.when(participationRequestService.getParticipationRequestsOwnEvents(1L, 1L))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/1/events/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void updateStatusForRequests_shouldReturnResult() throws Exception {
        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();
        request.setRequestIds(List.of(1L));
        request.setStatus(RequestState.CONFIRMED);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(
                List.of(ParticipationRequestDto.builder().id(1L).build()),
                List.of()
        );

        Mockito.when(participationRequestService.updateStatusForRequests(eq(1L), eq(1L), any()))
                .thenReturn(result);

        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", hasSize(1)));
    }

    @Test
    void updateStatusForRequests_shouldReturn400_whenFieldsInvalid() throws Exception {
        // Оба поля отсутствуют = невалидно
        EventRequestStatusUpdateRequest invalidRequest =
                new EventRequestStatusUpdateRequest(null, null);

        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}