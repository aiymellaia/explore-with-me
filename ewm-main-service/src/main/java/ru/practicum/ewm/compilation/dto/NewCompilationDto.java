package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    private Set<Long> events;
    private Boolean pinned;
    @NotBlank(message = "Title cannot be blank")
    private String title;
}