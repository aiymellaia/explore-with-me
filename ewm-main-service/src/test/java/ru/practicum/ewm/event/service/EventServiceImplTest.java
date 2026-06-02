package ru.practicum.ewm.event.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    @DisplayName("Создание: Ошибка, если дата начала раньше чем через 2 часа")
    void createEvent_InvalidDate_ThrowsException() {
        NewEventDto dto = NewEventDto.builder()
                .title("Test Event")
                .annotation("Annotation text at least 20 chars")
                .description("Description text at least 20 chars")
                .category(1L)
                .location(new LocationDto(55.75f, 37.61f))
                .eventDate(LocalDateTime.now().plusHours(1))
                .build();

        // Мокаем поиск пользователя и категории через anyLong(), чтобы избежать PotentialStubbingProblem
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(new Category()));

        assertThrows(ValidationException.class, () -> eventService.createEvent(1L, dto));
    }

    @Test
    @DisplayName("Редактирование автором: Ошибка, если статус PUBLISHED")
    void updateEvent_Published_ThrowsException() {
        Event event = Event.builder().state(EventState.PUBLISHED).initiator(new User(1L, "A", "b@c.ru")).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ValidationException.class, () ->
                eventService.updateEvent(1L, 1L, new ru.practicum.ewm.event.dto.UpdateEventUserRequest()));
    }

    @Test
    @DisplayName("Админ: Ошибка при публикации события не в статусе PENDING")
    void updateEventAdmin_NotPending_ThrowsException() {
        Event event = Event.builder().state(EventState.PUBLISHED).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest update = new UpdateEventAdminRequest();
        update.setStateAction(AdminStateAction.PUBLISH_EVENT);

        assertThrows(ValidationException.class, () -> eventService.updateEventAdmin(1L, update));
    }

    @Test
    @DisplayName("Админ: Ошибка при публикации, если до начала меньше 1 часа")
    void updateEventAdmin_TooSoon_ThrowsException() {
        Event event = Event.builder()
                .state(EventState.PENDING)
                .eventDate(LocalDateTime.now().plusMinutes(30)) // меньше часа
                .build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest update = new UpdateEventAdminRequest();
        update.setStateAction(AdminStateAction.PUBLISH_EVENT);

        assertThrows(ConflictException.class, () -> eventService.updateEventAdmin(1L, update));
    }
}