package ru.practicum.event.comment.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.comment.mapper.CommentMapper;
import ru.practicum.event.comment.model.*;
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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;
    private final CommentMapper commentMapper;
    private final EntityManager entityManager;

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentRequest newCommentRequest) {
        User author = userService.getEntityById(userId);
        Event event = eventService.getEntityById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Оставлять комментарий можно только к опубликованным событиям.");
        }

        Comment comment = Comment.builder()
                .text(newCommentRequest.getText())
                .author(author)
                .event(event)
                .created(LocalDateTime.now())
                .build();

        eventService.incrementCommentsCount(eventId); // при добавлении комментария увеличиваем счетчик
        commentRepository.save(comment);

        log.info("Пользователь {} добавил комментарий к событию '{}'", userId, event.getTitle());
        return commentMapper.toDto(comment);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest updateCommentRequest) {
        Comment existing = getEntityById(commentId);
        userService.getEntityById(userId);

        if (!existing.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Только автор комментария может его редактировать.");
        }

        commentMapper.update(updateCommentRequest, existing);
        commentRepository.save(existing);

        log.info("Пользователь {} обновил комментарий к событию '{}'", userId, existing.getEvent().getTitle());
        return commentMapper.toDto(existing);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        Comment existing = getEntityById(commentId);
        userService.getEntityById(userId);

        if (!existing.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Только автор комментария может его удалить.");
        }

        eventService.decrementCommentsCount(existing.getEvent().getId());
        commentRepository.deleteById(commentId);
        log.info("Пользователь {} удалил комментарий к событию '{}'", userId, existing.getEvent().getTitle());
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment existing =  getEntityById(commentId);
        commentRepository.deleteById(commentId);
        log.info("Комментарий удален модератором с id={} к событию '{}'", commentId, existing.getEvent().getTitle());
    }

    @Override
    public Comment getEntityById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден."));
    }

    @Override
    public List<CommentFullDto> getCommentsForModeration(AdminCommentSearchRequest request) {
        List<Comment> comments = commentRepository.findByAdminFilter(request);
        return comments.stream()
                .map(commentMapper::toFullDto)
                .toList();
    }

    @Override
    public List<CommentFullDto> getOwnComments(Long userId, UserCommentSearchRequest userCommentSearchRequest) {
        List<Comment> comments = commentRepository.findByUserFilter(userId, userCommentSearchRequest);
        return comments.stream()
                .map(commentMapper::toFullDto)
                .toList();
    }
}