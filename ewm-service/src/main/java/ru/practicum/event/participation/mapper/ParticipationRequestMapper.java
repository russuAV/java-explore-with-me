package ru.practicum.event.participation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.participation.model.ParticipationRequest;
import ru.practicum.event.participation.model.ParticipationRequestDto;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "event", expression = "java(request.getEvent().getId())")
    @Mapping(target = "requester", expression = "java(request.getRequester().getId())")
    ParticipationRequestDto toDto(ParticipationRequest request);
}
