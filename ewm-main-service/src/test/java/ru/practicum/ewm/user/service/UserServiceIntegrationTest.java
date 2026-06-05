package ru.practicum.ewm.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Интеграция: создание пользователя и его получение")
    void createUser_ValidData_SavesInDb() {
        UserDto dto = UserDto.builder()
                .name("Ivan")
                .email("i@i.ru")
                .build();

        UserDto created = userService.createUser(dto);

        assertAll(
                () -> assertNotNull(created.getId()),
                () -> assertEquals("Ivan", created.getName()),
                () -> assertEquals("i@i.ru", created.getEmail())
        );
    }

    @Test
    @DisplayName("Интеграция: получение списка пользователей с пагинацией")
    void getUsers_Pagination_ReturnsCorrectPage() {
        userService.createUser(new UserDto(null, "User 1", "u1@i.ru"));
        userService.createUser(new UserDto(null, "User 2", "u2@i.ru"));
        userService.createUser(new UserDto(null, "User 3", "u3@i.ru"));

        List<UserDto> users = userService.getUsers(null, 0, 2);

        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Интеграция: поиск по списку конкретных ID")
    void getUsers_WithIds_ReturnsOnlyRequested() {
        UserDto u1 = userService.createUser(UserDto.builder().name("User 1").email("u1@i.ru").build());
        UserDto u2 = userService.createUser(UserDto.builder().name("User 2").email("u2@i.ru").build());

        List<UserDto> result = userService.getUsers(List.of(u1.getId(), u2.getId()), 0, 10);

        assertEquals(2, result.size());

        List<String> names = result.stream().map(UserDto::getName).toList();
        assertTrue(names.contains("User 1"));
        assertTrue(names.contains("User 2"));
    }
}