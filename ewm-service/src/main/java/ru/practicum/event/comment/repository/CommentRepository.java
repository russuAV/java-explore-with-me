package ru.practicum.event.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.comment.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
}