package ru.practicum.event.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.comment.model.AdminCommentSearchRequest;
import ru.practicum.event.comment.model.CommentFullDto;
import ru.practicum.event.model.EventShortDto;
import ru.practicum.event.comment.service.CommentService;
import ru.practicum.user.model.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminCommentController.class)
class AdminCommentControllerTest {

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
    void deleteComment_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/comments/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(commentService, Mockito.times(1))
                .deleteCommentByAdmin(anyLong());
    }

    @Test
    void getCommentsForModeration_shouldReturnListOfComments() throws Exception {
        CommentFullDto comment1 = createCommentFullDto(
                1L, "Text 1", 1L, "User 1", 1L, "Event 1");
        CommentFullDto comment2 = createCommentFullDto(
                2L, "Text 2", 2L, "User 2", 2L, "Event 2");

        Mockito.when(commentService.getCommentsForModeration(any(AdminCommentSearchRequest.class)))
                .thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/admin/comments")
                        .param("users", "1,2")
                        .param("events", "1,2")
                        .param("rangeStart", "2023-01-01 00:00:00")
                        .param("rangeEnd", "2023-12-31 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(comment1.getId()), Long.class))
                .andExpect(jsonPath("$[0].text", is(comment1.getText())))
                .andExpect(jsonPath("$[0].author.id", is(comment1.getAuthor().getId()), Long.class))
                .andExpect(jsonPath("$[0].event.id", is(comment1.getEvent().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(comment2.getId()), Long.class))
                .andExpect(jsonPath("$[1].text", is(comment2.getText())));

        Mockito.verify(commentService, Mockito.times(1))
                .getCommentsForModeration(any(AdminCommentSearchRequest.class));
    }

    @Test
    void getCommentsForModeration_withInvalidParameters_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/comments")
                        .param("rangeStart", "invalid-date"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/admin/comments")
                        .param("rangeStart", "2023-01-02 00:00:00")
                        .param("rangeEnd", "2023-01-01 23:59:59"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCommentsForModeration_withEmptyParameters_shouldReturnAllComments() throws Exception {
        CommentFullDto comment = createCommentFullDto(
                1L, "Text", 1L, "User", 1L, "Event");

        Mockito.when(commentService.getCommentsForModeration(any(AdminCommentSearchRequest.class)))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/admin/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        Mockito.verify(commentService, Mockito.times(1))
                .getCommentsForModeration(any(AdminCommentSearchRequest.class));
    }
}