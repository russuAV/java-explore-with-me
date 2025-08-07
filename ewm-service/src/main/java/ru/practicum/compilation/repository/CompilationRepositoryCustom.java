package ru.practicum.compilation.repository;

import ru.practicum.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepositoryCustom {
    List<Compilation> findCompilations(Boolean pinned, int from, int size);
}