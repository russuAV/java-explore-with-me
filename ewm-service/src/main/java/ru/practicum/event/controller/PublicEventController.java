package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.event.model.EventFullDto;
import ru.practicum.event.model.EventShortDto;
import ru.practicum.event.model.PublicEventSearchRequest;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getPublicEvents(
            @Valid @ModelAttribute PublicEventSearchRequest request,
            HttpServletRequest httpRequest) {
        return eventService.getPublicEvents(request, httpRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto getPublishedEventById(
            @PathVariable("id") Long eventId,
            HttpServletRequest httpRequest) {
        return eventService.getPublishedEventById(eventId, httpRequest);
    }
}