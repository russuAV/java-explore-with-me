package ru.practicum.event.participation.service;

import ru.practicum.event.participation.model.EventRequestStatusUpdateRequest;
import ru.practicum.event.participation.model.EventRequestStatusUpdateResult;
import ru.practicum.event.participation.model.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getParticipationRequestsOwnEvents(Long userId, Long eventId);

    List<ParticipationRequestDto> getOwnRequests(Long userId);

    EventRequestStatusUpdateResult updateStatusForRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}