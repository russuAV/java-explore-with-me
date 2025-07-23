package ru.practicum.event.repository;

import ru.practicum.event.model.AdminEventSearchRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.PublicEventSearchRequest;

import java.util.List;

public interface EventRepositoryCustom {

    List<Event> findPublicEventsByFilter(PublicEventSearchRequest request);

    List<Event> findByAdminFilter(AdminEventSearchRequest request);
}