package ru.practicum.event.comment.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.category.model.Category;
import ru.practicum.event.comment.model.AdminCommentSearchRequest;
import ru.practicum.event.comment.model.Comment;
import ru.practicum.event.comment.model.UserCommentSearchRequest;
import ru.practicum.event.location.Location;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryCustomImplTest {

    @Autowired
    private CommentRepositoryCustomImpl repository;

    @Autowired
    private EntityManager em;

    private User user;
    private Event event;
    private Comment comment;
    private Category category;

    @BeforeEach
    void setup() {
        user = User.builder()
                .name("Author")
                .email("author@test.com")
                .build();
        em.persist(user);

        category = Category.builder()
                .name("Movie")
                .build();
        em.persist(category);

        event = Event.builder()
                .title("Test Event")
                .annotation("Odio sint delectus beatae nulla")
                .description("Odio sint delectus beatae nulla")
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .initiator(user)
                .paid(false)
                .location(new Location(1.0, 1.0))
                .category(category)
                .build();
        em.persist(event);

        comment = Comment.builder()
                .author(user)
                .event(event)
                .text("Test comment")
                .created(LocalDateTime.now())
                .build();
        em.persist(comment);

        em.flush();
    }

    @Test
    void findByAdminFilter_shouldReturnComment() {
        AdminCommentSearchRequest request = AdminCommentSearchRequest.builder()
                .eventIds(List.of(event.getId()))
                .authorIds(List.of(user.getId()))
                .rangeStart(LocalDateTime.now().minusMinutes(5))
                .rangeEnd(LocalDateTime.now().plusMinutes(5))
                .from(0)
                .size(10)
                .build();

        List<Comment> result = repository.findByAdminFilter(request);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");
    }

    @Test
    void findByUserFilter_shouldReturnComment() {
        UserCommentSearchRequest request = UserCommentSearchRequest.builder()
                .eventIds(List.of(event.getId()))
                .rangeStart(LocalDateTime.now().minusMinutes(5))
                .rangeEnd(LocalDateTime.now().plusMinutes(5))
                .from(0)
                .size(10)
                .build();

        List<Comment> result = repository.findByUserFilter(user.getId(), request);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");
    }
}