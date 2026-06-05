package ru.practicum.ewm.request.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock private RequestRepository requestRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    @DisplayName("Создание заявки: Ошибка, если автор подает заявку на свое событие")
    void createRequest_AuthorIsRequester_ThrowsException() {
        User user = new User(1L, "Ivan", "i@i.ru");
        Event event = Event.builder()
                .id(1L)
                .initiator(user) // Автор - сам Ivan
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(RuntimeException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    @DisplayName("Обновление статусов: Лимит достигнут, отклонение остальных заявок")
    void updateStatus_LimitReached_RejectPendingRequests() {
        User initiator = new User();
        initiator.setId(1L);

        User requester = new User();
        requester.setId(2L);

        Event event = Event.builder()
                .id(1L)
                .participantLimit(1)
                .requestModeration(true)
                .initiator(initiator)
                .build();

        ParticipationRequest req1 = ParticipationRequest.builder()
                .id(1L)
                .status(RequestStatus.PENDING)
                .event(event)
                .requester(requester)
                .build();

        ParticipationRequest req2 = ParticipationRequest.builder()
                .id(2L)
                .status(RequestStatus.PENDING)
                .event(event)
                .requester(requester)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllById(any())).thenReturn(List.of(req1, req2));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);
        when(requestRepository.findAllByEventIdAndStatus(1L, RequestStatus.PENDING)).thenReturn(List.of(req2));

        EventRequestStatusUpdateRequest update = new EventRequestStatusUpdateRequest(List.of(1L, 2L), RequestStatus.CONFIRMED);

        requestService.updateStatus(1L, 1L, update);

        assertEquals(RequestStatus.CONFIRMED, req1.getStatus());
        assertEquals(RequestStatus.REJECTED, req2.getStatus());
        verify(requestRepository, times(2)).saveAll(any());
    }
}