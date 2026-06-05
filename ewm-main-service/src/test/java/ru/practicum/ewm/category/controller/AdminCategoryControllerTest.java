package ru.practicum.ewm.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminCategoryController.class)
public class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createCategory_shouldReturnCreated() throws Exception {
        CategoryDto categoryDto = new CategoryDto(null, "Концерты");
        CategoryDto savedCategory = new CategoryDto(1L, "Концерты");

        when(categoryService.createCategory(any())).thenReturn(savedCategory);

        mvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(categoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Концерты"));
    }

    @Test
    void createCategory_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        CategoryDto categoryDto = new CategoryDto(null, ""); // Валидация @NotBlank

        mvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(categoryDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {
        mvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent()); // Ожидаемый статус для успешного удаления
    }
}