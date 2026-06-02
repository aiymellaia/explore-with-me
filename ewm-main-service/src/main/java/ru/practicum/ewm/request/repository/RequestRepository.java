package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus status);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByEventId(Long eventId);
}