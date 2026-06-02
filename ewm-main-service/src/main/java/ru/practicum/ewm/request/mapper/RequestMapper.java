package ru.practicum.ewm.request.mapper;

import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public static EventRequestStatusUpdateResult toUpdateResult(
            List<ParticipationRequest> confirmed,
            List<ParticipationRequest> rejected) {

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream()
                        .map(RequestMapper::toDto)
                        .collect(Collectors.toList()))
                .rejectedRequests(rejected.stream()
                        .map(RequestMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }
}