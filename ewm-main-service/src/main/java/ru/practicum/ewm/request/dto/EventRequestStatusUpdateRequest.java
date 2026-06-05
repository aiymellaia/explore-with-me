package ru.practicum.ewm.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    RequestStatus status;
}
