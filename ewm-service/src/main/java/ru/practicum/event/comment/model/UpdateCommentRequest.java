package ru.practicum.event.comment.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentRequest {

    @NotBlank
    @Length(min = 1, max = 2000)
    private String text;
}