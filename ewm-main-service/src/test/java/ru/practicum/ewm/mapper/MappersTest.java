package ru.practicum.ewm.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappersTest {

    @Test
    @DisplayName("CategoryMapper: корректное преобразование")
    void categoryMapperTest() {
        Category category = new Category(1L, "Концерты");
        CategoryDto dto = CategoryMapper.toCategoryDto(category);

        assertEquals(category.getId(), dto.getId());
        assertEquals(category.getName(), dto.getName());
    }

    @Test
    @DisplayName("UserMapper: корректное преобразование User в UserShortDto")
    void userMapperTest() {
        User user = new User(1L, "Ivan", "i@i.ru");
        UserShortDto dto = UserMapper.toUserShortDto(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
    }

    @Test
    @DisplayName("CompilationMapper: корректное преобразование со списком событий")
    void compilationMapperTest() {
        Category category = new Category(1L, "Театр");
        User user = new User(1L, "Ivan", "i@i.ru");
        Event event = Event.builder()
                .id(1L)
                .title("Спектакль")
                .category(category)
                .initiator(user)
                .eventDate(LocalDateTime.now())
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Подборка")
                .pinned(true)
                .events(Set.of(event))
                .build();

        CompilationDto dto = CompilationMapper.toDto(compilation);

        assertEquals(compilation.getTitle(), dto.getTitle());
        assertEquals(1, dto.getEvents().size());
        assertEquals("Спектакль", dto.getEvents().get(0).getTitle());
    }

    @Test
    @DisplayName("RequestMapper: преобразование в DTO и результат обновления")
    void requestMapperTest() {
        User user = new User(1L, "User", "u@u.ru");
        Event event = Event.builder().id(1L).build();

        ParticipationRequest request = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();

        ParticipationRequestDto dto = RequestMapper.toDto(request);
        assertEquals(request.getId(), dto.getId());
        assertEquals(request.getEvent().getId(), dto.getEvent());

        EventRequestStatusUpdateResult result = RequestMapper.toUpdateResult(List.of(request), List.of());
        assertEquals(1, result.getConfirmedRequests().size());
        assertTrue(result.getRejectedRequests().isEmpty());
    }


    @Test
    @DisplayName("EventMapper: корректный маппинг в Full и Short DTO")
    void eventMapperTest() {
        Category category = new Category(1L, "Театр");
        User initiator = new User(1L, "Ivan", "i@i.ru");

        Event event = Event.builder()
                .id(1L)
                .title("Спектакль")
                .annotation("Краткое описание")
                .description("Полное описание")
                .category(category)
                .initiator(initiator)
                .eventDate(LocalDateTime.now())
                .location(new Location(55.75f, 37.61f))
                .paid(true)
                .participantLimit(100)
                .build();

        EventFullDto fullDto = EventMapper.toEventFullDto(event);
        assertEquals(event.getTitle(), fullDto.getTitle());
        assertEquals(event.getCategory().getName(), fullDto.getCategory().getName());
        assertEquals(event.getInitiator().getName(), fullDto.getInitiator().getName());
        assertEquals(event.getLocation().getLat(), fullDto.getLocation().getLat());

        EventShortDto shortDto = EventMapper.toEventShortDto(event);
        assertEquals(event.getTitle(), shortDto.getTitle());
        assertEquals(event.getInitiator().getId(), shortDto.getInitiator().getId());
    }
}