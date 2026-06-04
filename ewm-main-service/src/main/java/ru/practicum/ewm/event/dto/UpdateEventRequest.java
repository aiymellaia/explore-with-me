package ru.practicum.ewm.event.dto;

import java.time.LocalDateTime;

public interface UpdateEventRequest {
    String getTitle();

    String getAnnotation();

    String getDescription();

    LocalDateTime getEventDate();

    LocationDto getLocation();

    Boolean getPaid();

    Integer getParticipantLimit();

    Boolean getRequestModeration();

    Long getCategory();
}