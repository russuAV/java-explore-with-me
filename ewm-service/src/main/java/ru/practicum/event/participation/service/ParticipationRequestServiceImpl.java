package ru.practicum.event.participation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.participation.mapper.ParticipationRequestMapper;
import ru.practicum.event.participation.repository.ParticipationRequestRepository;
import ru.practicum.event.participation.model.*;
import ru.practicum.event.service.EventService;
import ru.practicum.event.model.state.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final UserService userService;
    private final EventService eventService;
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userService.getEntityById(userId);
        Event event = eventService.getEntityById(eventId);

        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников события достигнут.");
        }

        if (participationRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Запрос на участие уже имеется!");
        }

        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now(ZoneOffset.UTC))
                .event(event)
                .requester(user)
                .status(RequestState.PENDING)
                .build();

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestState.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        if (!event.getRequestModeration() && event.getConfirmedRequests() < event.getParticipantLimit()) {
            request.setStatus(RequestState.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        ParticipationRequest created = participationRequestRepository.save(request);

        log.info("Заявка на участие в событии '{}' от пользователя id={} создана", event.getTitle(), userId);
        return participationRequestMapper.toDto(created);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userService.getEntityById(userId);

        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Данная заявка на участие в событие не найдена."));

        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь может отменить только свою заявку.");
        }

        if (participationRequest.getStatus() == RequestState.CONFIRMED) {
            eventService.decrementConfirmedRequests(participationRequest.getEvent().getId());
        }

        participationRequest.setStatus(RequestState.CANCELED);
        participationRequestRepository.save(participationRequest);

        log.info("Заявка на участие в событии '{}' отменена.", participationRequest.getEvent().getTitle());
        return participationRequestMapper.toDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsOwnEvents(Long userId, Long eventId) {
        userService.getEntityById(userId);
        Event event = eventService.getEntityById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Только инициатор события может увидеть запросы на участие");
        }

        List<ParticipationRequest> requests = participationRequestRepository
                .findAllByEventIdAndEvent_Initiator_Id(eventId, userId);

        return requests.stream()
                .map(participationRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getOwnRequests(Long userId) {
        userService.getEntityById(userId);
        List<ParticipationRequest> requests = participationRequestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(participationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusForRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {

        userService.getEntityById(userId);
        Event event = eventService.getEntityById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Только инициатор события может изменить статус запроса на участие");
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Нет свободных мест для участия в данном событии.");
        }

        if ((!event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждение заявок не требуется для данного события.");
        }

        List<ParticipationRequest> requests = participationRequestRepository
                .findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        if (requests.isEmpty()) {
            throw new NotFoundException("Заявки с указанными ID не найдены.");
        }

        // Проверяем, что все заявки в статусе PENDING
        if (requests.stream().anyMatch(r -> r.getStatus() != RequestState.PENDING)) {
            throw new ConflictException("Можно изменять только заявки со статусом PENDING.");
        }

        int confirmed = event.getConfirmedRequests();
        int limit = event.getParticipantLimit();

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        if (eventRequestStatusUpdateRequest.getStatus() == RequestState.CONFIRMED) {
            for (ParticipationRequest request : requests) {
                if (confirmed < limit) {
                    request.setStatus(RequestState.CONFIRMED);
                    confirmedRequests.add(request);
                    confirmed++;
                } else {
                    request.setStatus(RequestState.REJECTED);
                    rejectedRequests.add(request);
                }
            }
            eventService.incrementConfirmedRequests(eventId, confirmedRequests.size());
        } else if (eventRequestStatusUpdateRequest.getStatus() == RequestState.REJECTED) {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestState.REJECTED);
                rejectedRequests.add(request);
            }
        }

        participationRequestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream().map(participationRequestMapper::toDto).toList(),
                rejectedRequests.stream().map(participationRequestMapper::toDto).toList()
        );
    }
}