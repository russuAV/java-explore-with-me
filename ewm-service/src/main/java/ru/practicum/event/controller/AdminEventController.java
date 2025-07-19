package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.model.AdminEventSearchRequest;
import ru.practicum.event.model.EventFullDto;
import ru.practicum.event.model.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsByParams(@ModelAttribute AdminEventSearchRequest params) {
        return eventService.getEventsByParams(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        return eventService.updateByAdmin(eventId, updateEventAdminRequest);
    }
}