package ru.practicum.ewm.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Важно: откатывает изменения после каждого теста
class RequestServiceIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User initiator;
    private User requester;
    private Event event;

    @BeforeEach
    void setUp() {
        // Создаем данные в БД для каждого теста
        initiator = userRepository.save(new User(null, "Initiator", "i@i.ru"));
        requester = userRepository.save(new User(null, "Requester", "r@r.ru"));

        Category category = categoryRepository.save(new Category(null, "Test Category"));

        event = eventRepository.save(Event.builder()
                .title("Test Event")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .category(category)
                .initiator(initiator)
                .participantLimit(10)
                .requestModeration(true)
                .state(ru.practicum.ewm.event.model.EventState.PUBLISHED)
                .build());
    }

    @Test
    @DisplayName("Интеграционный тест: создание заявки успешно сохраняется в БД")
    void createRequest_Success() {
        ParticipationRequestDto result = requestService.createRequest(requester.getId(), event.getId());

        assertNotNull(result.getId());
        assertEquals(requester.getId(), result.getRequester());
        assertEquals(event.getId(), result.getEvent());
        assertEquals(RequestStatus.PENDING, result.getStatus());
    }
}