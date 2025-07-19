package ru.practicum.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.location.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank
    @Length(max = 2000, min = 20)
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    @Length(max = 7000, min = 20)
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private Location location;

    private Boolean paid = false;

    @Builder.Default
    @Min(0)
    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotBlank
    @Length(max = 120, min = 3)
    private String title;
}