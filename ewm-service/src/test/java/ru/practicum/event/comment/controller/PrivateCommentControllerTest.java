package ru.practicum.event.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.comment.model.*;
import ru.practicum.event.comment.service.CommentService;
import ru.practicum.event.model.EventShortDto;
import ru.practicum.user.model.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrivateCommentController.class)
class PrivateCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private UserShortDto createUserShortDto(Long id, String name) {
        return UserShortDto.builder()
                .id(id)
                .name(name)
                .build();
    }

    private EventShortDto createEventShortDto(Long id, String title) {
        return EventShortDto.builder()
                .id(id)
                .title(title)
                .build();
    }

    private CommentDto createCommentDto(Long id, String text) {
        return CommentDto.builder()
                .id(id)
                .text(text)
                .build();
    }

    private CommentFullDto createCommentFullDto(Long id, String text, Long userId, String userName,
                                                Long eventId, String eventTitle) {
        return CommentFullDto.builder()
                .id(id)
                .text(text)
                .author(createUserShortDto(userId, userName))
                .event(createEventShortDto(eventId, eventTitle))
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        NewCommentRequest request = new NewCommentRequest("Test comment text");
        CommentDto response = createCommentDto(1L, "Test comment text");

        Mockito.when(commentService.addComment(anyLong(), anyLong(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/users/1/comments/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(response.getText())));

        Mockito.verify(commentService, Mockito.times(1))
                .addComment(eq(1L), eq(1L), any());
    }

    @Test
    void addComment_withInvalidText_shouldReturnBadRequest() throws Exception {
        NewCommentRequest emptyTextRequest = new NewCommentRequest("");
        NewCommentRequest nullTextRequest = new NewCommentRequest(null);
        NewCommentRequest tooLongTextRequest = new NewCommentRequest("a".repeat(2001));

        mockMvc.perform(post("/users/1/comments/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyTextRequest)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/1/comments/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullTextRequest)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/1/comments/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooLongTextRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_shouldReturnUpdatedComment() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated text");
        CommentDto response = createCommentDto(1L, "Updated text");

        Mockito.when(commentService.updateComment(anyLong(), anyLong(), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/users/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(response.getText())));

        Mockito.verify(commentService, Mockito.times(1))
                .updateComment(eq(1L), eq(1L), any());
    }

    @Test
    void deleteComment_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1/comments/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(commentService, Mockito.times(1))
                .deleteComment(eq(1L), eq(1L));
    }

    @Test
    void getOwnComments_shouldReturnListOfComments() throws Exception {
        CommentFullDto comment1 = createCommentFullDto(
                1L, "Text 1", 1L, "User 1", 1L, "Event 1");
        CommentFullDto comment2 = createCommentFullDto(
                2L, "Text 2", 1L, "User 1", 2L, "Event 2");

        Mockito.when(commentService.getOwnComments(anyLong(), any()))
                .thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/users/1/comments")
                        .param("events", "1,2")
                        .param("rangeStart", "2023-01-01 00:00:00")
                        .param("rangeEnd", "2023-12-31 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(comment1.getId()), Long.class))
                .andExpect(jsonPath("$[0].text", is(comment1.getText())))
                .andExpect(jsonPath("$[1].id", is(comment2.getId()), Long.class))
                .andExpect(jsonPath("$[1].text", is(comment2.getText())));

        Mockito.verify(commentService, Mockito.times(1))
                .getOwnComments(eq(1L), any());
    }

    @Test
    void getOwnComments_withInvalidParameters_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users/1/comments")
                        .param("rangeStart", "invalid-date"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/1/comments")
                        .param("rangeStart", "2023-01-02 00:00:00")
                        .param("rangeEnd", "2023-01-01 23:59:59"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnComments_withEmptyParameters_shouldReturnAllUserComments() throws Exception {
        CommentFullDto comment = createCommentFullDto(
                1L, "Text", 1L, "User", 1L, "Event");

        Mockito.when(commentService.getOwnComments(anyLong(), any()))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/users/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        Mockito.verify(commentService, Mockito.times(1))
                .getOwnComments(eq(1L), any());
    }
}