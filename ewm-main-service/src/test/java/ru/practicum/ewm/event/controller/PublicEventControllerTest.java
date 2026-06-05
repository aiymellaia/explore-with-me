package ru.practicum.ewm.event.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.service.EventService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicEventController.class)
public class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    @DisplayName("GET Получение события по ID - Успех (200 OK)")
    void getEvent_Success() throws Exception {
        long eventId = 1L;
        EventFullDto eventFullDto = EventFullDto.builder()
                .id(eventId)
                .title("Test Event")
                .build();

        when(eventService.getEvent(eq(eventId), any())).thenReturn(eventFullDto);

        mockMvc.perform(get("/events/{id}", eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Test Event"));
    }

    @Test
    @DisplayName("GET Поиск событий - Успех (200 OK)")
    void getEvents_Success() throws Exception {
        when(eventService.getEvents(anyString(), any(), anyBoolean(), any(), any(), anyBoolean(), anyString(), anyInt(), anyInt(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/events")
                        .param("text", "поиск")
                        .param("paid", "true")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}