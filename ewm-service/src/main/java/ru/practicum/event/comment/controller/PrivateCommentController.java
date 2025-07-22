package ru.practicum.event.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.comment.model.*;
import ru.practicum.event.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentRequest newCommentRequest) {
        return commentService.addComment(userId, eventId, newCommentRequest);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest updateCommentRequest
            ) {
        return commentService.updateComment(userId, commentId, updateCommentRequest);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
    commentService.deleteComment(userId, commentId);
    }

    @GetMapping
    public List<CommentFullDto> getOwnComments(
            @PathVariable Long userId,
            @Valid @ModelAttribute UserCommentSearchRequest request) {
        return commentService.getOwnComments(userId, request);
    }
}