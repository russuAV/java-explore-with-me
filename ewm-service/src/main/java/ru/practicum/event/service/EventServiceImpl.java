package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.model.state.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.event.model.state.UpdateEventState.PUBLISH_EVENT;
import static ru.practicum.event.model.state.UpdateEventState.REJECT_EVENT;


@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final CategoryService categoryService;
    private final StatsClient statsClient;

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        if (!newEventDto.getEventDate().minusHours(2).isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Событие не удовлетворяет правилам создания");
        }
        User creator = userService.getEntityById(userId);

        Event event = eventMapper.toEvent(newEventDto);
        event.setCategory(categoryService.getEntityById(newEventDto.getCategory()));

        event.setInitiator(creator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setConfirmedRequests(0);

        Event created = eventRepository.save(event);

        log.info("Создано событие '{}'", created.getTitle());
        return eventMapper.toEventFullDto(created);
    }

    @Override
    public EventFullDto updateByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userService.getEntityById(userId);

        Event existing = getEntityById(eventId);

        if (!existing.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Событие не принадлежит пользователю id=" + userId);
        }

        if (updateEventUserRequest.getEventDate() != null &&
                (!updateEventUserRequest.getEventDate().minusHours(2).isAfter(LocalDateTime.now()))) {
            throw new BadRequestException("Событие не удовлетворяет правилам создания");
        }

        if (existing.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Запрос составлен некорректно");
        }

        eventMapper.updateEventFromDto(updateEventUserRequest, existing);

        // Обновление категории
        if (updateEventUserRequest.getCategory() != null) {
            existing.setCategory(categoryService.getEntityById(updateEventUserRequest.getCategory()));
        }

        // Обновление даты
        if (updateEventUserRequest.getEventDate() != null) {
            existing.setEventDate(updateEventUserRequest.getEventDate());
        }

        // Обработка изменения статуса
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW -> existing.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> existing.setState(EventState.PENDING);
            }
        }

        Event saved = eventRepository.save(existing);

        log.info("Событие '{}' обновлено.", saved.getTitle());
        return eventMapper.toEventFullDto(saved);
    }

    @Override
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateDto) {
        Event existing = getEntityById(eventId);

        if (existing.getState().equals(EventState.CANCELED)) {
            throw new ConflictException("Нельзя опубликовать отмененное событие");
        }

        if (existing.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие уже опубликовано.");
        }

        if (updateDto.getEventDate() != null &&
                !updateDto.getEventDate().minusHours(1).isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Дата начала события должна быть не ранее чем за час от публикации");
        }

        if (updateDto.getStateAction() != null) {
            if (updateDto.getStateAction() == PUBLISH_EVENT) {
                if (existing.getState() != EventState.PENDING) {
                    throw new ForbiddenException("Публикация возможна только из состояния PENDING");
                }
                existing.setPublishedOn(LocalDateTime.now());
                existing.setState(EventState.PUBLISHED);
            } else if (updateDto.getStateAction() == REJECT_EVENT) {
                if (existing.getState() == EventState.PUBLISHED) {
                    throw new ForbiddenException("Нельзя отклонить опубликованное событие");
                }
                existing.setState(EventState.CANCELED);
            }
        }

        eventMapper.updateEventFromAdminDto(updateDto, existing);

        return eventMapper.toEventFullDto(eventRepository.save(existing));
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userService.getEntityById(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").descending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        userService.getEntityById(userId);

        Event event = getEntityById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Событие не принадлежит пользователю id=" + userId);
        }

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public Event getEntityById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Событие с id=" + eventId + " не найдено."));
    }

    @Transactional
    public void decrementConfirmedRequests(Long eventId) {
        Event event = getEntityById(eventId);
        if (event.getConfirmedRequests() > 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
            log.info("Количество подтвержденных заявок стало меньше. Теперь {}", event.getConfirmedRequests());
        }
    }

    @Transactional
    public void incrementConfirmedRequests(Long eventId, int count) {
        Event event = getEntityById(eventId);
        event.setConfirmedRequests(event.getConfirmedRequests() + count);
        eventRepository.save(event);
        log.info("Количество подтвержденных заявок стало больше. Теперь {}", event.getConfirmedRequests());
    }

    @Override
   public Set<Event> findAllById(Set<Long> ids) {
        return eventRepository.findByIdIn(ids);
    }

    @Override
    public List<EventFullDto> getEventsByParams(AdminEventSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getFrom() / request.getSize(), request.getSize());

        List<EventState> states = null;
        if (request.getStates() != null) {
            states = request.getStates().stream()
                    .map(s -> {
                        try {
                            return EventState.valueOf(s);
                        } catch (IllegalArgumentException e) {
                            throw new BadRequestException("Некорректное значение состояния: " + s);
                        }
                    }).toList();
        }

        LocalDateTime start = request.getRangeStart() != null
                ? request.getRangeStart()
                : LocalDateTime.now();

        LocalDateTime end = request.getRangeEnd() != null
                ? request.getRangeEnd()
                : LocalDateTime.now().plusYears(1);

        Page<Event> page = eventRepository.findByAdminParams(
                request.getUsers(),
                states,
                request.getCategories(),
                start,
                end,
                pageable
        );

        return page.stream()
                .map(eventMapper::toEventFullDto)
                .toList();
    }

    @Override
    public List<EventShortDto> getPublicEvents(PublicEventSearchRequest request, HttpServletRequest httpRequest) {
        sendStatistics(httpRequest);

        Pageable pageable = PageRequest.of(
                request.getFrom() / request.getSize(),
                request.getSize(),
                Sort.by(request.getSort().equals("VIEWS") ? "views" : "eventDate")
        );

        LocalDateTime start = request.getRangeStart() != null
                ? request.getRangeStart()
                : LocalDateTime.now();

        LocalDateTime end = request.getRangeEnd() != null
                ? request.getRangeEnd()
                : LocalDateTime.now().plusYears(1);

        Page<Event> page = eventRepository.findPublicEvents(
                request.getText(),
                request.getCategories(),
                request.getPaid(),
                start,
                end,
                request.getOnlyAvailable() != null ? request.getOnlyAvailable() : false,
                pageable
        );

        return page.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }


    @Override
    public EventFullDto getPublishedEventById(Long eventId, HttpServletRequest httpRequest) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или не опубликовано"));

        sendStatistics(httpRequest);

        List<ViewStatsDto> stats = statsClient.getStats(
                event.getPublishedOn().atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().minusSeconds(1),
                LocalDateTime.now().atZone(ZoneId.systemDefault())
                                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                List.of("/events/" + eventId),
                true
        );

        int views = stats.isEmpty() ? 0 : stats.getFirst().getHits().intValue();
        event.setViews(views);

        return eventMapper.toEventFullDto(event);
    }

    private void sendStatistics(HttpServletRequest httpRequest) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(httpRequest.getRequestURI())
                .ip(httpRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.saveHit(hitDto);
    }
}