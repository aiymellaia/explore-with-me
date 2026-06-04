package ru.practicum.ewm.category.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Удаление: Ошибка, если категория связана с событием")
    void deleteCategory_WithEvents_ThrowsConflictException() {
        // 1. Создаем категорию
        CategoryDto categoryDto = new CategoryDto(null, "Концерты");
        CategoryDto savedCategory = categoryService.createCategory(categoryDto);

        // 2. Создаем пользователя и событие, привязанное к этой категории
        User initiator = userRepository.save(new User(null, "Ivan", "i@i.ru"));
        eventRepository.save(Event.builder()
                .title("Концерт")
                .annotation("Аннотация")
                .description("Описание")
                .eventDate(LocalDateTime.now().plusDays(1))
                .category(categoryRepository.findById(savedCategory.getId()).get())
                .initiator(initiator)
                .location(new Location(55.75f, 37.61f))
                .build());

        // 3. Пытаемся удалить категорию и ожидаем ConflictException
        assertThrows(ConflictException.class, () -> categoryService.deleteCategory(savedCategory.getId()));
    }

    @Test
    @DisplayName("Обновление: корректное изменение имени категории")
    void updateCategory_ValidName_UpdatesSuccessfully() {
        CategoryDto savedCategory = categoryService.createCategory(new CategoryDto(null, "Старое имя"));
        CategoryDto updateDto = new CategoryDto(null, "Новое имя");

        CategoryDto updated = categoryService.updateCategory(savedCategory.getId(), updateDto);

        assertEquals("Новое имя", updated.getName());
        assertEquals(savedCategory.getId(), updated.getId());
    }
}