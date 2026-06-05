package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.EventSpecification;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.utils.OffsetBasedPageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;

    private static final String APP_NAME = "ewm-main-service";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата начала события должна быть не раньше чем через два часа от текущего момента");
        }

        Event event = Event.builder()
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .category(category)
                .initiator(user)
                .eventDate(newEventDto.getEventDate())
                .location(new Location(
                        newEventDto.getLocation().getLat(),
                        newEventDto.getLocation().getLon()
                ))
                .paid(newEventDto.getPaid() != null && newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .build();

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest update) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Редактировать можно только свои события");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя изменить опубликованное событие");
        }

        updateEventFields(event, update);

        if (update.getUserStateAction() != null) {
            if (update.getUserStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (update.getUserStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        } else if (event.getState().equals(EventState.CANCELED)) {
            event.setState(EventState.PENDING);
        }

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest update) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (update.getStateAction() != null) {
            if (update.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new ConflictException("Событие можно опубликовать только в состоянии ожидания");
                }
                event.setState(EventState.PUBLISHED);
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Дата начала события должна быть не раньше чем через час от даты публикации");
                }
                event.setPublishedOn(LocalDateTime.now());
            } else if (update.getStateAction() == AdminStateAction.REJECT_EVENT) {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, update);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Параметры from и size должны быть положительными");
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        List<Event> events = eventRepository.findAll(
                EventSpecification.filterEvents(users, states, categories, rangeStart, rangeEnd),
                new OffsetBasedPageRequest(from, size, Sort.unsorted())
        ).getContent();

        if (events.isEmpty()) return Collections.emptyList();

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedMap = requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));

        String[] uris = eventIds.stream().map(id -> "/events/" + id).toArray(String[]::new);

        String start = events.stream()
                .map(this::getStartDateTimeForStats)
                .min(String::compareTo)
                .orElse(LocalDateTime.now().format(formatter));

        Map<String, Long> viewsMap = getStatsMap(start, LocalDateTime.now().format(formatter), uris);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = EventMapper.toEventFullDto(event);
                    dto.setConfirmedRequests(confirmedMap.getOrDefault(event.getId(), 0L));
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие еще не опубликовано");
        }

        statsClient.addHit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(formatter))
                .build());

        List<ViewStatsDto> statsList = Collections.emptyList();
        try {
            String start = getStartDateTimeForStats(event);
            String end = LocalDateTime.now().format(formatter);

            Object statsResponse = statsClient.getStats(
                    start,
                    end,
                    new String[]{request.getRequestURI()},
                    true);

            if (statsResponse != null) {
                statsList = objectMapper.convertValue(statsResponse, new TypeReference<List<ViewStatsDto>>() {
                });
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении статистики: " + e.getMessage());
        }

        EventFullDto dto = EventMapper.toEventFullDto(event);
        long views = statsList.stream()
                .filter(stats -> stats.getUri().equals(request.getRequestURI()))
                .mapToLong(ViewStatsDto::getHits)
                .findFirst()
                .orElse(0L);

        dto.setViews(views);
        dto.setConfirmedRequests(getConfirmedRequests(eventId));

        return dto;
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                         String sort, Integer from, Integer size, HttpServletRequest request) {

        if (from < 0 || size <= 0) {
            throw new ValidationException("Параметры from и size должны быть положительными");
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        statsClient.addHit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(formatter))
                .build());

        List<Event> events = eventRepository.findAll(
                EventSpecification.filterPublicEvents(text, categories, paid, rangeStart, rangeEnd),
                new OffsetBasedPageRequest(from, size, Sort.unsorted())
        ).getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedMap = requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = EventMapper.toEventShortDto(event);
                    dto.setConfirmedRequests(confirmedMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());

        String[] uris = result.stream().map(dto -> "/events/" + dto.getId()).toArray(String[]::new);
        String start = getMinStartDateTimeForStats(events);
        String end = LocalDateTime.now().format(formatter);

        Map<String, Long> viewsMap = getStatsMap(start, end, uris);

        result.forEach(dto -> dto.setViews(viewsMap.getOrDefault("/events/" + dto.getId(), 0L)));

        if ("VIEWS".equals(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());

        } else if ("EVENT_DATE".equals(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        return result;
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Параметры пагинации должны быть положительными");
        }

        return eventRepository.findAllByInitiatorId(userId, new OffsetBasedPageRequest(from, size, Sort.unsorted()))
                .stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Вы не являетесь инициатором этого события");
        }
        return EventMapper.toEventFullDto(event);
    }

    private String getStartDateTimeForStats(Event event) {
        if (event.getPublishedOn() != null) {
            return event.getPublishedOn().format(formatter);
        }
        return event.getCreatedOn().format(formatter);
    }

    private String getMinStartDateTimeForStats(List<Event> events) {
        return events.stream()
                .map(this::getStartDateTimeForStats)
                .min(String::compareTo)
                .orElse(LocalDateTime.now().format(formatter));
    }

    private Map<String, Long> getStatsMap(String start, String end, String[] uris) {
        try {
            Object statsResponse = statsClient.getStats(start, end, uris, true);
            if (statsResponse != null) {
                List<ViewStatsDto> statsList = objectMapper.convertValue(statsResponse, new TypeReference<>() {
                });
                return statsList.stream()
                        .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении статистики: " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    private void updateEventFields(Event event, UpdateEventRequest update) {
        boolean isAdmin = (update instanceof UpdateEventAdminRequest);
        applyCommonFields(event, update, isAdmin);
    }

    private void applyCommonFields(Event event, UpdateEventRequest update, boolean isAdmin) {

        if (update.getTitle() != null) {
            if (update.getTitle().length() < 3 || update.getTitle().length() > 120) {
                throw new ValidationException("Длина заголовка должна быть от 3 до 120 символов");
            }
            event.setTitle(update.getTitle());
        }

        if (update.getAnnotation() != null) {
            if (update.getAnnotation().length() < 20 || update.getAnnotation().length() > 2000) {
                throw new ValidationException("Длина аннотации должна быть от 20 до 2000 символов");
            }
            event.setAnnotation(update.getAnnotation());
        }

        if (update.getDescription() != null) {
            if (update.getDescription().length() < 20 || update.getDescription().length() > 7000) {
                throw new ValidationException("Длина описания должна быть от 20 до 7000 символов");
            }
            event.setDescription(update.getDescription());
        }

        if (update.getEventDate() != null) {
            // Убрали лишний параметр isUpdate, так как метод вызывается только при обновлении
            LocalDateTime minAllowedDate = isAdmin ? LocalDateTime.now().plusHours(1)
                    : LocalDateTime.now().plusHours(2);

            if (update.getEventDate().isBefore(minAllowedDate)) {
                throw new ValidationException("Дата начала события должна быть не раньше чем через "
                        + (isAdmin ? "час" : "два часа") + " от текущего момента");
            }
            event.setEventDate(update.getEventDate());
        }

        if (update.getLocation() != null) {
            LocationDto loc = update.getLocation();
            if (event.getLocation() == null) {
                event.setLocation(new Location(loc.getLat(), loc.getLon()));
            } else {
                event.getLocation().setLat(loc.getLat());
                event.getLocation().setLon(loc.getLon());
            }
        }

        if (update.getPaid() != null) event.setPaid(update.getPaid());
        if (update.getParticipantLimit() != null) event.setParticipantLimit(update.getParticipantLimit());
        if (update.getRequestModeration() != null) event.setRequestModeration(update.getRequestModeration());

        if (update.getCategory() != null) {
            Category newCategory = categoryRepository.findById(update.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + update.getCategory() + " не найдена"));
            event.setCategory(newCategory);
        }
    }
}