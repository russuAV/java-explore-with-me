package ru.practicum.event.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.comment.model.AdminCommentSearchRequest;
import ru.practicum.event.comment.model.CommentFullDto;
import ru.practicum.event.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) { // возможность администратором удалять комменты
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping
    public List<CommentFullDto> getCommentsForModeration(
            @Valid @ModelAttribute AdminCommentSearchRequest adminCommentSearchRequest) {
        return commentService.getCommentsForModeration(adminCommentSearchRequest);
    }
}
