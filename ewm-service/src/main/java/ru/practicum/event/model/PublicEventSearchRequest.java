package ru.practicum.event.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicEventSearchRequest {
    private String text;

    private List<Long> categories;

    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable = false;
    private String sort = "EVENT_DATE";

    @PositiveOrZero
    private int from = 0;

    @Positive
    private int size = 10;

    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    public boolean isValidDateRange() {
        return rangeStart == null || rangeEnd == null || !rangeStart.isAfter(rangeEnd);
    }
}