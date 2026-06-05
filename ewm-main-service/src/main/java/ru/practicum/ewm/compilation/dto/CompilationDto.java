package ru.practicum.ewm.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CompilationDto {
    private Long id;
    @Builder.Default
    private Boolean pinned = false;
    private String title;
    @Builder.Default
    private List<EventShortDto> events = new ArrayList<>();
}