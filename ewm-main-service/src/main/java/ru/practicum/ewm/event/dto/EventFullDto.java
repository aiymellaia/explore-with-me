package ru.practicum.ewm.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.user.dto.UserShortDto;
import java.time.LocalDateTime;

@Data
@Builder
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private UserShortDto initiator;
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;
    private Long views;
    private Long confirmedRequests;
}