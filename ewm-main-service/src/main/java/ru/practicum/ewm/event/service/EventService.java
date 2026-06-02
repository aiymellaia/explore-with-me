package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    // --- Публичные методы (Public API) ---
    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                  String sort, Integer from, Integer size, HttpServletRequest request);

    EventFullDto getEvent(Long eventId, HttpServletRequest request);

    // --- Приватные методы (Private API) ---
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEvent);

    // --- Методы для администратора (Admin API) ---
    // states теперь List<EventState> (бизнес-объект), а не String
    List<EventFullDto> getEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEvent);
}