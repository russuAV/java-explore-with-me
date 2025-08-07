package ru.practicum.event.comment.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCommentSearchRequest {
    private List<Long> eventIds;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @PositiveOrZero
    private int from = 0;

    @PositiveOrZero
    private int size = 10;

    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    public boolean isValidRange() {
        return rangeStart == null || rangeEnd == null || !rangeStart.isAfter(rangeEnd);
    }
}
