package ru.practicum.ewm.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.service.RequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateEventController.class)
public class PrivateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private RequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    // Корректная настройка Jackson без потери десериализации Lombok DTO-классов
    @TestConfiguration
    static class JacksonTestConfig {
        @Bean
        @Primary
        public ObjectMapper testObjectMapper(Jackson2ObjectMapperBuilder builder) {
            // Используем стандартный билдер Spring, который знает про структуру наших DTO
            return builder
                    .simpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .serializers(new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .deserializers(new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
        }
    }

    // =========================================================================
    // POST /users/{userId}/events (Создание события)
    // =========================================================================

    @Test
    @DisplayName("POST Создание события - Ошибка (400 BAD_REQUEST) невалидная дата начала")
    void createEvent_BadRequest_InvalidDate() throws Exception {
        NewEventDto newEvent = NewEventDto.builder()
                .title("Valid Title of Event")
                .annotation("Valid annotation description for event longer than twenty chars")
                .description("Valid description details for event longer than twenty chars")
                .category(1L)
                .location(new LocationDto(55.754167f, 37.62f))
                .eventDate(LocalDateTime.now().plusMinutes(30)) // Менее 2 часов до события
                .build();

        when(eventService.createEvent(anyLong(), any(NewEventDto.class)))
                .thenThrow(new ValidationException("Дата начала события должна быть не ранее чем через два часа"));

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Дата начала события должна быть не ранее чем через два часа"));
    }

    @Test
    @DisplayName("POST Создание события - Ошибка (400 BAD_REQUEST) слишком короткие поля")
    void createEvent_BadRequest_ValidationFailed() throws Exception {
        NewEventDto newEvent = NewEventDto.builder()
                .title("Short")
                .annotation("Too short")
                .description("Short desc")
                .category(1L)
                .location(new LocationDto(55.754167f, 37.62f))
                .eventDate(LocalDateTime.now().plusHours(3)) // Дата корректна, чтобы не падать на этапе десериализации
                .build();

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // =========================================================================
    // PATCH /users/{userId}/events/{eventId} (Обновление события пользователем)
    // =========================================================================

    @Test
    @DisplayName("PATCH Обновление события пользователем - Ошибка (409 CONFLICT) событие уже опубликовано")
    void updateEventUser_Conflict_AlreadyPublished() throws Exception {
        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("Updated Title")
                .build();

        when(eventService.updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class)))
                .thenThrow(new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания модерации"));

        mockMvc.perform(patch("/users/1/events/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Изменить можно только отмененные события или события в состоянии ожидания модерации"));
    }
}