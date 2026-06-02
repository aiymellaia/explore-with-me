package ru.practicum.ewm.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import java.time.LocalDateTime;

@Data
@Builder
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests; // Количество одобренных заявок
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private Long views; // Количество просмотров
}