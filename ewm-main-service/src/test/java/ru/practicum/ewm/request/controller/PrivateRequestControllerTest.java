package ru.practicum.ewm.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.service.RequestService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrivateRequestController.class)
public class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(10L)
                .requester(2L)
                .status(RequestStatus.PENDING)
                .build();
    }

    // =========================================================================
    // БЛОК: Создание заявки (POST /users/{userId}/requests)
    // =========================================================================

    @Test
    @DisplayName("POST Создание заявки - Успех (201 CREATED)")
    void createRequest_Success() throws Exception {
        when(requestService.createRequest(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$.requester").value(requestDto.getRequester()));

        verify(requestService, times(1)).createRequest(2L, 10L);
    }

    @Test
    @DisplayName("POST Создание заявки - Ошибка (409 CONFLICT) Дубликат заявки")
    void createRequest_Conflict_Duplicate() throws Exception {
        when(requestService.createRequest(anyLong(), anyLong()))
                .thenThrow(new ConflictException("Заявка уже существует"));

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.reason").value("Integrity constraint has been violated."))
                .andExpect(jsonPath("$.message").value("Заявка уже существует"));
    }

    @Test
    @DisplayName("POST Создание заявки - Ошибка (409 CONFLICT) Заявка на свое событие")
    void createRequest_Conflict_OwnEvent() throws Exception {
        when(requestService.createRequest(anyLong(), anyLong()))
                .thenThrow(new ConflictException("Нельзя подать заявку на свое событие"));

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Нельзя подать заявку на свое событие"));
    }

    @Test
    @DisplayName("POST Создание заявки - Ошибка (409 CONFLICT) Событие не опубликовано")
    void createRequest_Conflict_NotPublished() throws Exception {
        when(requestService.createRequest(anyLong(), anyLong()))
                .thenThrow(new ConflictException("Нельзя подать заявку на неопубликованное событие"));

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Нельзя подать заявку на неопубликованное событие"));
    }

    @Test
    @DisplayName("POST Создание заявки - Ошибка (409 CONFLICT) Лимит участников достигнут")
    void createRequest_Conflict_LimitReached() throws Exception {
        when(requestService.createRequest(anyLong(), anyLong()))
                .thenThrow(new ConflictException("Лимит участников достигнут"));

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Лимит участников достигнут"));
    }

    // =========================================================================
    // БЛОК: Отмена заявки (PATCH /users/{userId}/requests/{requestId}/cancel)
    // =========================================================================

    @Test
    @DisplayName("PATCH Отмена заявки - Успех (200 OK)")
    void cancelRequest_Success() throws Exception {
        requestDto.setStatus(RequestStatus.CANCELED);
        when(requestService.cancelRequest(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvc.perform(patch("/users/2/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(requestService, times(1)).cancelRequest(2L, 1L);
    }

    @Test
    @DisplayName("PATCH Отмена заявки - Ошибка (404 NOT_FOUND) Заявка не найдена")
    void cancelRequest_NotFound() throws Exception {
        when(requestService.cancelRequest(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Заявка не найдена"));

        mockMvc.perform(patch("/users/2/requests/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Заявка не найдена"));
    }

    @Test
    @DisplayName("PATCH Отмена заявки - Ошибка (400 BAD_REQUEST) Чужая заявка")
    void cancelRequest_BadRequest_NotOwner() throws Exception {
        when(requestService.cancelRequest(anyLong(), anyLong()))
                .thenThrow(new ValidationException("Вы не можете отменить чужую заявку"));

        mockMvc.perform(patch("/users/3/requests/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Вы не можете отменить чужую заявку"));
    }
}