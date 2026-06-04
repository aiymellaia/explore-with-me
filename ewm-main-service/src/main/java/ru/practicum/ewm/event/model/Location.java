package ru.practicum.ewm.event.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Builder.Default
    private Float lat = 0.0f;

    @Builder.Default
    private Float lon = 0.0f;
}