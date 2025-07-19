package ru.practicum.event.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.participation.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventIdAndEvent_Initiator_Id(Long eventId, Long initiatorId);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);
}