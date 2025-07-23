package ru.practicum.event.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.event.comment.mapper.CommentMapper;
import ru.practicum.event.comment.model.Comment;
import ru.practicum.event.comment.model.CommentDto;
import ru.practicum.event.comment.model.NewCommentRequest;
import ru.practicum.event.comment.model.UpdateCommentRequest;
import ru.practicum.event.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Event event;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@email.com");
        user.setName("User");

        event = new Event();
        event.setId(1L);
        event.setTitle("Event Title");
        event.setState(EventState.PUBLISHED);

        comment = Comment.builder()
                .id(1L)
                .text("Comment text")
                .author(user)
                .event(event)
                .created(LocalDateTime.now())
                .build();

        commentDto = new CommentDto(1L, "Comment text", "User", LocalDateTime.now());
    }

    @Test
    void addComment_shouldCreateNewComment() {
        NewCommentRequest request = new NewCommentRequest("New comment");
        when(userService.getEntityById(anyLong())).thenReturn(user);
        when(eventService.getEntityById(anyLong())).thenReturn(event);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDto);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.addComment(1L, 1L, request);

        assertNotNull(result);
        assertEquals(commentDto.getId(), result.getId());
        verify(eventService).incrementCommentsCount(1L);
    }

    @Test
    void addComment_shouldThrowConflictForUnpublishedEvent() {
        event.setState(EventState.PENDING);
        NewCommentRequest request = new NewCommentRequest("New comment");
        when(userService.getEntityById(anyLong())).thenReturn(user);
        when(eventService.getEntityById(anyLong())).thenReturn(event);

        assertThrows(ConflictException.class, () ->
                commentService.addComment(1L, 1L, request));
    }

    @Test
    void updateComment_shouldUpdateCommentText() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated text");
        when(userService.getEntityById(anyLong())).thenReturn(user);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(
                new CommentDto(1L, "Updated text", "User", LocalDateTime.now()));

        CommentDto result = commentService.updateComment(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Updated text", result.getText());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_shouldThrowForbiddenForNonAuthor() {
        User otherUser = new User();
        otherUser.setId(2L);
        UpdateCommentRequest request = new UpdateCommentRequest("Updated text");
        when(userService.getEntityById(anyLong())).thenReturn(otherUser);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        assertThrows(ForbiddenException.class, () ->
                commentService.updateComment(2L, 1L, request));
    }

    @Test
    void deleteComment_shouldDeleteComment() {
        when(userService.getEntityById(anyLong())).thenReturn(user);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 1L);

        verify(commentRepository).deleteById(1L);
        verify(eventService).decrementCommentsCount(1L);
    }

    @Test
    void deleteCommentByAdmin_shouldDeleteWithoutChecks() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        commentService.deleteCommentByAdmin(1L);

        verify(commentRepository).deleteById(1L);
        verify(eventService, never()).decrementCommentsCount(anyLong());
    }

    @Test
    void getEntityById_shouldReturnComment() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        Comment result = commentService.getEntityById(1L);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
    }

    @Test
    void getEntityById_shouldThrowNotFound() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                commentService.getEntityById(1L));
    }
}