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
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ConflictException;

import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminEventController.class)
public class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class JacksonTestConfig {
        @Bean
        @Primary
        public ObjectMapper testObjectMapper(Jackson2ObjectMapperBuilder builder) {
            return builder
                    .serializers(new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .deserializers(new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
        }
    }

    @Test
    @DisplayName("PATCH Админ: Публикация события - Успех (200 OK)")
    void updateEventAdmin_PublishSuccess() throws Exception {
        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        EventFullDto eventFullDto = EventFullDto.builder()
                .id(1L)
                .title("Test Event")
                .state(EventState.PUBLISHED)
                .build();

        when(eventService.updateEventAdmin(anyLong(), any(UpdateEventAdminRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
    }

    @Test
    @DisplayName("PATCH Админ: Публикация события - Ошибка (409 CONFLICT) если не PENDING")
    void updateEventAdmin_Conflict_IfNotPending() throws Exception {
        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        String errorMessage = "Событие можно публиковать только если оно в состоянии ожидания публикации";

        when(eventService.updateEventAdmin(anyLong(), any(UpdateEventAdminRequest.class)))
                .thenThrow(new ConflictException(errorMessage));

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}