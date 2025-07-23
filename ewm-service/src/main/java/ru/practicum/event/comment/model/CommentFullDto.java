package ru.practicum.event.comment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.EventShortDto;
import ru.practicum.user.model.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentFullDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private EventShortDto event;
    private LocalDateTime created;
}