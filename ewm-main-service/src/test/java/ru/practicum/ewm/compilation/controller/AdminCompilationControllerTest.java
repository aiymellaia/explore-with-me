package ru.practicum.ewm.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompilationAdminController.class)
public class AdminCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST Создание подборки - Успех (201 CREATED)")
    void createCompilation_Success() throws Exception {
        NewCompilationDto newDto = NewCompilationDto.builder()
                .title("Летняя подборка")
                .pinned(true)
                .events(Set.of(1L, 2L))
                .build();

        CompilationDto responseDto = CompilationDto.builder()
                .id(1L)
                .title("Летняя подборка")
                .pinned(true)
                .build();

        when(compilationService.createCompilation(any(NewCompilationDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Летняя подборка"));
    }

    @Test
    @DisplayName("POST Создание подборки - Ошибка (400 BAD_REQUEST) пустой title")
    void createCompilation_BadRequest_EmptyTitle() throws Exception {
        NewCompilationDto newDto = NewCompilationDto.builder()
                .title("") // Пустой title нарушает @NotBlank
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH Обновление подборки - Успех (200 OK)")
    void updateCompilation_Success() throws Exception {
        long compId = 1L;
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("Обновленное название")
                .build();

        CompilationDto responseDto = CompilationDto.builder()
                .id(compId)
                .title("Обновленное название")
                .pinned(false)
                .build();

        when(compilationService.updateCompilation(eq(compId), any(UpdateCompilationRequest.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/admin/compilations/{compId}", compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Обновленное название"));
    }
}