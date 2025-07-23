package ru.practicum.event.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.comment.model.Comment;
import ru.practicum.event.comment.model.CommentDto;
import ru.practicum.event.comment.model.NewCommentRequest;
import ru.practicum.event.comment.model.UpdateCommentRequest;
import ru.practicum.event.comment.repository.CommentRepository;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class CommentServiceImplIT {

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User author;
    private Event publishedEvent;
    private Category category;


    @BeforeEach
    void setUp() {

        author = userRepository.save(User.builder()
                .email("author@example.com")
                .name("Author")
                .build());
        category = categoryRepository.save(new Category(null, "Concert"));


        publishedEvent = eventRepository.save(Event.builder()
                .title("Published Event")
                .annotation("Odio sint delectus beatae nulla")
                .description("Odio sint delectus beatae nulla")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new Location(55.75, 37.62))
                .paid(false)
                .category(category)
                .participantLimit(0)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .initiator(author)
                .build());
    }

    @Test
    void addComment_shouldSaveCommentWithAllFields() {
        NewCommentRequest request = new NewCommentRequest("Test comment");

        CommentDto result = commentService.addComment(author.getId(), publishedEvent.getId(), request);

        assertNotNull(result.getId());
        assertEquals("Test comment", result.getText());

        Comment savedComment = commentRepository.findById(result.getId()).orElseThrow();
        assertEquals(author.getId(), savedComment.getAuthor().getId());
        assertEquals(publishedEvent.getId(), savedComment.getEvent().getId());
        assertNotNull(savedComment.getCreated());
    }

    @Test
    void updateComment_shouldUpdateOnlyText() {
        Comment comment = commentRepository.save(Comment.builder()
                .text("Original text")
                .author(author)
                .event(publishedEvent)
                .created(LocalDateTime.now())
                .build());

        UpdateCommentRequest request = new UpdateCommentRequest("Updated text");
        CommentDto result = commentService.updateComment(author.getId(), comment.getId(), request);

        assertEquals("Updated text", result.getText());

        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertEquals("Updated text", updatedComment.getText());
        assertEquals(comment.getCreated(), updatedComment.getCreated());
        assertEquals(comment.getAuthor().getId(), updatedComment.getAuthor().getId());
        assertEquals(comment.getEvent().getId(), updatedComment.getEvent().getId());
    }

    @Test
    void deleteComment_shouldRemoveComment() {
        Comment comment = commentRepository.save(Comment.builder()
                .text("To be deleted")
                .author(author)
                .event(publishedEvent)
                .created(LocalDateTime.now())
                .build());

        commentService.deleteComment(author.getId(), comment.getId());

        assertFalse(commentRepository.existsById(comment.getId()));
    }

    @Test
    void getEntityById_shouldReturnCorrectComment() {
        Comment comment = commentRepository.save(Comment.builder()
                .text("Test comment")
                .author(author)
                .event(publishedEvent)
                .created(LocalDateTime.now())
                .build());

        Comment result = commentService.getEntityById(comment.getId());

        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getAuthor().getId(), result.getAuthor().getId());
        assertEquals(comment.getEvent().getId(), result.getEvent().getId());
    }

    @Test
    void addComment_shouldFailForUnpublishedEvent() {
        publishedEvent.setState(EventState.CANCELED);

        NewCommentRequest request = new NewCommentRequest("Test comment");

        assertThrows(ConflictException.class, () ->
                commentService.addComment(author.getId(), publishedEvent.getId(), request));

        assertEquals(0, commentRepository.count());
    }

    @Test
    void updateComment_shouldFailForNonAuthor() {
        User otherUser = userRepository.save(User.builder()
                .email("other@example.com")
                .name("Other User")
                .build());

        Comment comment = commentRepository.save(Comment.builder()
                .text("Original text")
                .author(author)
                .event(publishedEvent)
                .created(LocalDateTime.now())
                .build());

        UpdateCommentRequest request = new UpdateCommentRequest("Updated text");

        assertThrows(ForbiddenException.class, () ->
                commentService.updateComment(otherUser.getId(), comment.getId(), request));

        Comment notUpdated = commentRepository.findById(comment.getId()).orElseThrow();
        assertEquals("Original text", notUpdated.getText());
    }

    @Test
    void deleteComment_shouldFailForNonAuthor() {
        User otherUser = userRepository.save(User.builder()
                .email("other@example.com")
                .name("Other User")
                .build());

        Comment comment = commentRepository.save(Comment.builder()
                .text("To be deleted")
                .author(author)
                .event(publishedEvent)
                .created(LocalDateTime.now())
                .build());

        assertThrows(ForbiddenException.class, () ->
                commentService.deleteComment(otherUser.getId(), comment.getId()));

        assertTrue(commentRepository.existsById(comment.getId()));
    }
}