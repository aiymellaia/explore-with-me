package ru.practicum.ewm.compilation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock private CompilationRepository compilationRepository;
    @Mock private EventRepository eventRepository;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    @Test
    @DisplayName("Создание подборки: должна корректно сохранять список событий")
    void createCompilation_WithEvents_SavesCorrectly() {
        Category category = new Category(1L, "Концерты");
        User initiator = new User(1L, "Ivan", "ivan@test.com");

        Set<Long> eventIds = Set.of(1L, 2L);
        NewCompilationDto newDto = new NewCompilationDto();
        newDto.setTitle("Лучшие события");
        newDto.setEvents(eventIds);
        newDto.setPinned(true);

        Event event1 = Event.builder()
                .id(1L)
                .title("Event 1")
                .category(category)
                .initiator(initiator)
                .build();

        Event event2 = Event.builder()
                .id(2L)
                .title("Event 2")
                .category(category)
                .initiator(initiator)
                .build();

        List<Event> foundEvents = List.of(event1, event2);

        when(eventRepository.findAllById(eventIds)).thenReturn(foundEvents);

        when(compilationRepository.save(any(Compilation.class))).thenAnswer(i -> i.getArguments()[0]);

        CompilationDto result = compilationService.createCompilation(newDto);

        assertNotNull(result);
        assertEquals(2, result.getEvents().size());
        verify(eventRepository, times(1)).findAllById(eventIds);
        verify(compilationRepository, times(1)).save(any(Compilation.class));
    }
}