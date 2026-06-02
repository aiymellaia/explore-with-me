package ru.practicum.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.stats.service.StatsService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
public class StatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService statsService;

    private final String timestamp = "2022-09-06 11:00:23";

    @Test
    void addHit_shouldReturn201() throws Exception {
        EndpointHitDto hitDto = new EndpointHitDto(
                0L,
                "ewm-main-service",
                "/events/1",
                "192.163.0.1",
                timestamp
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(hitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getStats_shouldReturn200() throws Exception {
        mvc.perform(get("/stats")
                        .param("start", timestamp)
                        .param("end", "2022-09-06 12:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}