package ru.practicum.compilation.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventService eventService;
    private final StatsClient statsClient;


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
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilations.stream()
                .map(this::enrichCompilationDtoWithViews)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId, HttpServletRequest request) {
        Compilation compilation = getEntityById(compId);

        return enrichCompilationDtoWithViews(compilation);
    }

    private CompilationDto enrichCompilationDtoWithViews(Compilation compilation) {
        Set<Event> events = compilation.getEvents();

        if (events == null || events.isEmpty()) {
            return compilationMapper.toDto(compilation);
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        // Находим самую раннюю дату события, чтобы корректно задать диапазон
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        List<ViewStatsDto> stats = statsClient.getStats(start, LocalDateTime.now(), uris, false);

        Map<Long, Integer> viewsMap = stats.stream()
                .collect(Collectors.toMap(
                        s -> Long.parseLong(s.getUri().replace("/events/", "")),
                        s -> s.getHits().intValue()
                ));

        events.forEach(e -> e.setViews(viewsMap.getOrDefault(e.getId(), 0)));

        return compilationMapper.toDto(compilation);
    }
}