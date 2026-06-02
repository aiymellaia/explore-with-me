package ru.practicum.ewm.category.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Успешное создание категории")
    void createCategory_Success() {
        CategoryDto dto = CategoryDto.builder().name("Концерты").build();
        Category category = new Category(1L, "Концерты");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.createCategory(dto);

        assertEquals("Концерты", result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Обновление категории - ошибка, если не найдена")
    void updateCategory_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(1L, new CategoryDto(1L, "Новое имя")));
    }

    @Test
    @DisplayName("Удаление категории - ошибка, если есть связанные события")
    void deleteCategory_ConflictWithEvents() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true); // Есть события!

        assertThrows(ConflictException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Удаление категории - успех")
    void deleteCategory_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false); // Нет событий

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }
}