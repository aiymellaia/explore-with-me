package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class CommentController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid NewCommentDto newCommentDto) {
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long commentId,
            @RequestBody @Valid NewCommentDto newCommentDto) {
        return commentService.updateComment(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId, false);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(
            @RequestHeader(USER_ID_HEADER) Long adminId,
            @PathVariable Long commentId) {
        commentService.deleteComment(adminId, commentId, true);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getCommentsByEvent(@PathVariable Long eventId) {
        return commentService.getCommentsByEvent(eventId);
    }
}
