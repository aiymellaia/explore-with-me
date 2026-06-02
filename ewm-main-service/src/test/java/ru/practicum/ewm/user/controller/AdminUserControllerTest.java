package ru.practicum.ewm.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.exception.ErrorHandler;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import(ErrorHandler.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST Создание пользователя - Успех (201)")
    void createUser_Success() throws Exception {
        UserDto userDto = UserDto.builder().name("John").email("john@mail.com").build();
        UserDto savedUser = UserDto.builder().id(1L).name("John").email("john@mail.com").build();

        when(userService.createUser(any())).thenReturn(savedUser);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    @DisplayName("GET Получение пользователей - Успех (200)")
    void getUsers_Success() throws Exception {
        when(userService.getUsers(anyList(), anyInt(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("DELETE Удаление пользователя - Успех (204)")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET Ошибка (400) при некорректном 'from'")
    void getUsers_BadRequest_NegativeFrom() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "-5"))
                .andExpect(status().isBadRequest());
    }
}