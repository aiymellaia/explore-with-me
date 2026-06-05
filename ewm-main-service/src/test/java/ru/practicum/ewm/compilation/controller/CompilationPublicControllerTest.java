package ru.practicum.ewm.compilation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompilationPublicController.class)
public class CompilationPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    @Test
    @DisplayName("GET Получение всех подборок - Успех (200 OK)")
    void getCompilations_Success() throws Exception {
        CompilationDto dto = CompilationDto.builder()
                .id(1L)
                .title("Подборка для теста")
                .build();

        when(compilationService.getCompilations(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Подборка для теста"));
    }

    @Test
    @DisplayName("GET Получение подборки по ID - Успех (200 OK)")
    void getCompilationById_Success() throws Exception {
        long compId = 1L;
        CompilationDto dto = CompilationDto.builder()
                .id(compId)
                .title("Подборка для теста")
                .build();

        when(compilationService.getCompilationById(compId)).thenReturn(dto);

        mockMvc.perform(get("/compilations/{compId}", compId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compId))
                .andExpect(jsonPath("$.title").value("Подборка для теста"));
    }

    @Test
    @DisplayName("Обработка ошибок - 404 Not Found должен возвращать ApiError")
    void handleNotFound_ShouldReturnApiError() throws Exception {
        when(compilationService.getCompilationById(anyLong()))
                .thenThrow(new NotFoundException("Подборка не найдена"));

        mockMvc.perform(get("/compilations/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("Подборка не найдена"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}