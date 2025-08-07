package ru.practicum.compilation.service;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventService eventService;
    private final StatsClient statsClient;
    private final EntityManager entityManager;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        Set<Event> events = new HashSet<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events.addAll(eventService.findAllById(newCompilationDto.getEvents()));
        }

        compilation.setEvents(events);
        Compilation saved = compilationRepository.save(compilation);

        log.info("Подборка добавлена");
        return compilationMapper.toDto(saved);
    }


    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation existing = getEntityById(compId);

        compilationMapper.updateCompilationFromDto(updateCompilationRequest, existing);

        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventService.findAllById(updateCompilationRequest.getEvents()));
            existing.setEvents(events);
        }

        compilationRepository.save(existing);

        log.info("Подборка обновлена");
        return compilationMapper.toDto(existing);
    }

    @Override
    public void delete(Long compId) {
        Compilation compilation = getEntityById(compId);
        log.info("Подборка удалена");
        compilationRepository.delete(compilation);
    }

    @Override
    public Compilation getEntityById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена или недоступна"));
    }


    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size, HttpServletRequest request) {
        List<Compilation> compilations = compilationRepository.findCompilations(pinned, from, size);
        Map<Long, Integer> viewsMap = getAllViewsForCompilations(compilations);

        compilations.forEach(compilation -> {
            compilation.getEvents().forEach(event -> {
                event.setViews(viewsMap.getOrDefault(event.getId(), 0));
            });
        });

        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId, HttpServletRequest request) {
        Compilation compilation = getEntityById(compId);
        Map<Long, Integer> viewsMap = getAllViewsForCompilations(List.of(compilation));

        compilation.getEvents().forEach(event ->
                event.setViews(viewsMap.getOrDefault(event.getId(), 0))
        );

        return compilationMapper.toDto(compilation);
    }

    private Map<Long, Integer> getAllViewsForCompilations(List<Compilation> compilations) {
        if (compilations == null || compilations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Event> allEvents = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .toList();

        if (allEvents.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = allEvents.stream()
                .map(event -> "/events/" + event.getId())
                .distinct()
                .toList();

        LocalDateTime start = allEvents.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusMonths(1));

        List<ViewStatsDto> stats = statsClient.getStats(start, LocalDateTime.now(), uris, false);

        return stats.stream().collect(Collectors.toMap(
                s -> Long.parseLong(s.getUri().replace("/events/", "")),
                s -> s.getHits().intValue()
        ));
    }
}