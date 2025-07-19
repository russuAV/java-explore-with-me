package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.model.*;

import java.util.List;
import java.util.Set;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getEventById(Long userId, Long eventId);

    Event getEntityById(Long eventId);

    EventFullDto updateByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    void decrementConfirmedRequests(Long eventId);

    void incrementConfirmedRequests(Long eventId, int count);

    Set<Event> findAllById(Set<Long> ids);

    List<EventFullDto> getEventsByParams(AdminEventSearchRequest params);

    List<EventShortDto> getPublicEvents(PublicEventSearchRequest request, HttpServletRequest httpRequest);

    EventFullDto getPublishedEventById(Long eventId, HttpServletRequest httpRequest);

}