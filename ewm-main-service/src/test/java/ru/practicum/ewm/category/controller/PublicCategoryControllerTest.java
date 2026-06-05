package ru.practicum.ewm.category.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicCategoryController.class)
public class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET Получение списка категорий - Успех (200 OK)")
    void getCategories_Success() throws Exception {
        CategoryDto categoryDto = new CategoryDto(1L, "Концерты");

        when(categoryService.getCategories(anyInt(), anyInt())).thenReturn(List.of(categoryDto));

        mvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Концерты"));
    }

    @Test
    @DisplayName("GET Получение категории по ID - Успех (200 OK)")
    void getCategory_Success() throws Exception {
        long catId = 1L;
        CategoryDto categoryDto = new CategoryDto(catId, "Концерты");

        when(categoryService.getCategory(catId)).thenReturn(categoryDto);

        mvc.perform(get("/categories/{catId}", catId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catId))
                .andExpect(jsonPath("$.name").value("Концерты"));
    }

    @Test
    @DisplayName("GET Получение категорий - Ошибка (400) при некорректных параметрах")
    void getCategories_BadRequest_NegativeFrom() throws Exception {
        mvc.perform(get("/categories")
                        .param("from", "-1") // Некорректное значение
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}