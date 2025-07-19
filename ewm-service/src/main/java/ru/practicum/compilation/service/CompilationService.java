package ru.practicum.compilation.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto newCompilationDto);

    CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void delete(Long compId);

    Compilation getEntityById(Long compId);

    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size, HttpServletRequest request);

    CompilationDto getCompilationById(Long compId, HttpServletRequest request);

}