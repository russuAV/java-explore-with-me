package ru.practicum.compilation.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private List<CompilationDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}