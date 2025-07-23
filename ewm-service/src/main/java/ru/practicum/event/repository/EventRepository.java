package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.state.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event>,
        EventRepositoryCustom {
    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            SELECT e FROM Event e
            WHERE (:users IS NULL OR e.initiator.id IN :users)
            AND (:states IS NULL OR e.state IN :states)
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate >= :rangeStart)
            AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate <= :rangeEnd)
            """)
    Page<Event> findByAdminParams(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Event e
            WHERE e.state = 'PUBLISHED'
            AND (:text IS NULL
                 OR e.annotation ILIKE %:text%
                 OR e.description ILIKE %:text%)
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:paid IS NULL OR e.paid = :paid)
            AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate >= :rangeStart)
            AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate <= :rangeEnd)
            AND (:onlyAvailable = FALSE
            OR (e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit))
            """)
    Page<Event> findPublicEvents(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
            );

    Optional<Event> findByIdAndState(Long id, EventState state);

    Set<Event> findByIdIn(Set<Long> ids);

    @Query("SELECT e FROM Event e WHERE e.initiator.id = :userId ORDER BY e.id DESC LIMIT :size OFFSET :from")
    List<Event> findUserEventsWithOffset(@Param("userId") Long userId,
                                         @Param("from") int from,
                                         @Param("size") int size);

}
