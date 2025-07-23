package ru.practicum.event.comment.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.event.comment.model.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    void toComment_shouldMapNewCommentRequestToComment() {
        NewCommentRequest request = new NewCommentRequest("Test comment text");
        Comment comment = commentMapper.toComment(request);

        assertNotNull(comment);
        assertEquals("Test comment text", comment.getText());
        assertNull(comment.getId());
        assertNull(comment.getAuthor());
        assertNull(comment.getEvent());
    }

    @Test
    void toDto_shouldMapCommentToCommentDtoWithAuthorName() {
        User author = User.builder()
                .id(1L)
                .name("Test User")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Test comment")
                .author(author)
                .created(LocalDateTime.now())
                .build();

        CommentDto dto = commentMapper.toDto(comment);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test comment", dto.getText());
        assertEquals("Test User", dto.getAuthorName());
    }

    @Test
    void update_shouldUpdateOnlyCommentText() {
        Comment comment = Comment.builder()
                .id(1L)
                .text("Old text")
                .author(User.builder().id(1L).build())
                .event(Event.builder().id(1L).build())
                .created(LocalDateTime.now())
                .build();

        Comment updatedComment = commentMapper.update(
                new UpdateCommentRequest("New text"),
                comment
        );

        assertSame(comment, updatedComment);
        assertEquals(1L, updatedComment.getId());
        assertEquals("New text", updatedComment.getText());
        assertNotNull(updatedComment.getAuthor());
        assertNotNull(updatedComment.getEvent());
        assertNotNull(updatedComment.getCreated());
    }

    @Test
    void toFullDto_shouldMapAllFieldsCorrectly() {
        User author = User.builder()
                .id(1L)
                .name("Test User")
                .build();

        Event event = Event.builder()
                .id(1L)
                .title("Test Event")
                .build();

        LocalDateTime created = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Full test comment")
                .author(author)
                .event(event)
                .created(created)
                .build();

        CommentFullDto fullDto = commentMapper.toFullDto(comment);

        assertNotNull(fullDto);
        assertEquals(1L, fullDto.getId());
        assertEquals("Full test comment", fullDto.getText());

        assertNotNull(fullDto.getAuthor());
        assertEquals(1L, fullDto.getAuthor().getId());
        assertEquals("Test User", fullDto.getAuthor().getName());

        assertNotNull(fullDto.getEvent());
        assertEquals(1L, fullDto.getEvent().getId());
        assertEquals("Test Event", fullDto.getEvent().getTitle());

        assertEquals(created, fullDto.getCreated());
    }

    @Test
    void toFullDto_withNullFields_shouldMapWithoutErrors() {
        Comment comment = Comment.builder()
                .id(1L)
                .text("Comment with null fields")
                .created(LocalDateTime.now())
                .build();

        CommentFullDto fullDto = commentMapper.toFullDto(comment);

        assertNotNull(fullDto);
        assertEquals(1L, fullDto.getId());
        assertEquals("Comment with null fields", fullDto.getText());
        assertNull(fullDto.getAuthor());
        assertNull(fullDto.getEvent());
        assertNotNull(fullDto.getCreated());
    }
}