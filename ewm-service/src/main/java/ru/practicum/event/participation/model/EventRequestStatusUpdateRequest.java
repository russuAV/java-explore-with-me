package ru.practicum.event.participation.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotNull
    private List<Long> requestIds;

    @NotNull
    private RequestState status;
}
