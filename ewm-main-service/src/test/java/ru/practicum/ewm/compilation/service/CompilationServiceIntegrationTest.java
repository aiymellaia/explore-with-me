package ru.practicum.ewm.compilation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CompilationServiceIntegrationTest {

    @Autowired
    private CompilationService compilationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Event event;

    @BeforeEach
    void setUp() {
        User initiator = userRepository.save(new User(null, "Ivan", "i@i.ru"));
        Category category = categoryRepository.save(new Category(null, "Театр"));

        event = eventRepository.save(Event.builder()
                .title("Спектакль")
                .annotation("Аннотация")
                .description("Описание")
                .eventDate(LocalDateTime.now().plusDays(1))
                .category(category)
                .initiator(initiator)
                .build());
    }

    @Test
    @DisplayName("Интеграция: создание подборки с привязкой событий")
    void createCompilation_WithEvents_SavesInDb() {
        NewCompilationDto newDto = new NewCompilationDto();
        newDto.setTitle("Лучшая подборка");
        newDto.setEvents(Set.of(event.getId()));
        newDto.setPinned(true);

        CompilationDto result = compilationService.createCompilation(newDto);

        assertNotNull(result.getId());
        assertEquals(1, result.getEvents().size());
        assertEquals("Спектакль", result.getEvents().get(0).getTitle());
    }

    @Test
    @DisplayName("Интеграция: обновление подборки (смена списка событий)")
    void updateCompilation_UpdatesEventsCorrectly() {
        // Создаем подборку
        NewCompilationDto newDto = new NewCompilationDto();
        newDto.setTitle("Старая подборка");
        newDto.setEvents(Set.of(event.getId()));
        CompilationDto created = compilationService.createCompilation(newDto);

        // Обновляем (передаем пустой список событий)
        UpdateCompilationRequest update = new UpdateCompilationRequest();
        update.setEvents(Set.of()); // Удаляем все события из подборки
        update.setTitle("Новое название");

        CompilationDto updated = compilationService.updateCompilation(created.getId(), update);

        assertEquals("Новое название", updated.getTitle());
        assertTrue(updated.getEvents().isEmpty());
    }
}