package ru.practicum.ewm.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EventServiceIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private StatsClient statsClient;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(null, "Ivan", "i@i.ru"));
        category = categoryRepository.save(new Category(null, "Концерты"));
    }

    @Test
    @DisplayName("Интеграция: создание события с валидными данными")
    void createEvent_ValidData_ReturnsFullDto() {
        NewEventDto dto = new NewEventDto();
        dto.setTitle("Тестовый концерт");
        dto.setAnnotation("Краткое описание");
        dto.setDescription("Полное описание");
        dto.setCategory(category.getId());
        dto.setEventDate(LocalDateTime.now().plusHours(3));
        dto.setLocation(new LocationDto(55.0f, 37.0f));

        EventFullDto result = eventService.createEvent(user.getId(), dto);

        assertNotNull(result.getId());
        assertEquals(EventState.PENDING, result.getState());
        assertEquals("Тестовый концерт", result.getTitle());
    }

    @Test
    @DisplayName("Интеграция: обновление статуса администратором")
    void updateEventAdmin_Publish_ChangesState() {
        // Создаем событие
        NewEventDto dto = new NewEventDto();
        dto.setTitle("Концерт для публикации");
        dto.setAnnotation("...");
        dto.setDescription("...");
        dto.setCategory(category.getId());
        dto.setEventDate(LocalDateTime.now().plusHours(5));
        dto.setLocation(new LocationDto(55.0f, 37.0f));
        EventFullDto created = eventService.createEvent(user.getId(), dto);

        // Обновляем через админа
        UpdateEventAdminRequest update = new UpdateEventAdminRequest();
        update.setStateAction(AdminStateAction.PUBLISH_EVENT);

        EventFullDto published = eventService.updateEventAdmin(created.getId(), update);

        assertEquals(EventState.PUBLISHED, published.getState());
        assertNotNull(published.getPublishedOn());
    }
}