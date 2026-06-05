package ru.practicum.ewm.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Успешное создание пользователя")
    void createUser_Success() {
        UserDto userDto = UserDto.builder().name("Ivan").email("ivan@test.com").build();
        User user = new User(1L, "Ivan", "ivan@test.com");

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Удаление пользователя - ошибка, если не найден")
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Получение списка пользователей с пагинацией")
    void getUsers_WithPagination() {
        User user = new User(1L, "Ivan", "ivan@test.com");
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Создание пользователя - ошибка, если email уже существует")
    void createUser_ConflictEmail() {
        UserDto userDto = UserDto.builder().name("Ivan").email("ivan@test.com").build();

        // Имитируем, что репозиторий выбрасывает исключение при нарушении целостности
        when(userRepository.save(any(User.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Email already exists"));

        // Проверяем, что сервис пробрасывает это исключение дальше (или можно обернуть в свое, но чаще всего пробрасывают это)
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> userService.createUser(userDto));

        verify(userRepository, times(1)).save(any(User.class));
    }
}