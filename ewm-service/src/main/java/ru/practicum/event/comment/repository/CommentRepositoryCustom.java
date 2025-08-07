package ru.practicum.event.comment.repository;

import ru.practicum.event.comment.model.AdminCommentSearchRequest;
import ru.practicum.event.comment.model.Comment;
import ru.practicum.event.comment.model.UserCommentSearchRequest;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findByAdminFilter(AdminCommentSearchRequest request);

    List<Comment> findByUserFilter(Long userId, UserCommentSearchRequest request);
}
