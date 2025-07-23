package ru.practicum.event.comment.service;

import ru.practicum.event.comment.model.*;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentRequest newCommentRequest);

    void deleteComment(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest updateCommentRequest);

    Comment getEntityById(Long commentId);

    List<CommentFullDto> getCommentsForModeration(AdminCommentSearchRequest adminCommentSearchRequest);

    List<CommentFullDto> getOwnComments(Long userId, UserCommentSearchRequest userCommentSearchRequest);
}