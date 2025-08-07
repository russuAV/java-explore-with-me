package ru.practicum.event.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.AdminEventSearchRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.PublicEventSearchRequest;
import ru.practicum.event.model.state.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final EntityManager entityManager;

    @Override
    public List<Event> findPublicEventsByFilter(PublicEventSearchRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (request.getText() != null) {
            Predicate annotationLike = cb.like(cb.lower(root.get("annotation")), "%"
                    + request.getText().toLowerCase() + "%");
            Predicate descriptionLike = cb.like(cb.lower(root.get("description")), "%"
                    + request.getText().toLowerCase() + "%");
            predicates.add(cb.or(annotationLike, descriptionLike));
        }

        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            predicates.add(root.get("category").get("id").in(request.getCategories()));
        }

        if (request.getPaid() != null) {
            predicates.add(cb.equal(root.get("paid"), request.getPaid()));
        }

        predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

        LocalDateTime start = request.getRangeStart() != null ? request.getRangeStart() : LocalDateTime.now();
        LocalDateTime end = request.getRangeEnd() != null ? request.getRangeEnd() : LocalDateTime.now().plusYears(1);
        predicates.add(cb.between(root.get("eventDate"), start, end));

        if (Boolean.TRUE.equals(request.getOnlyAvailable())) {
            predicates.add(cb.or(
                    cb.equal(root.get("participantLimit"), 0),
                    cb.greaterThan(root.get("participantLimit"), root.get("confirmedRequests"))
            ));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Сортировка
        if ("COMMENTS".equals(request.getSort())) {
            query.orderBy(cb.desc(root.get("commentsCount")));
        } else if ("VIEWS".equals(request.getSort())) {
            query.orderBy(cb.desc(root.get("views")));
        } else {
            query.orderBy(cb.desc(root.get("eventDate")));
        }

        // Пагинация вручную
        return entityManager.createQuery(query)
                .setFirstResult(request.getFrom())
                .setMaxResults(request.getSize())
                .getResultList();
    }

    @Override
    public List<Event> findByAdminFilter(AdminEventSearchRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (request.getUsers() != null && !request.getUsers().isEmpty()) {
            predicates.add(root.get("initiator").get("id").in(request.getUsers()));
        }

        if (request.getStates() != null && !request.getStates().isEmpty()) {
            List<EventState> states = request.getStates().stream()
                    .map(EventState::valueOf)
                    .toList();
            predicates.add(root.get("state").in(states));
        }

        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            predicates.add(root.get("category").get("id").in(request.getCategories()));
        }

        LocalDateTime start = request.getRangeStart() != null
                ? request.getRangeStart()
                : LocalDateTime.now();

        LocalDateTime end = request.getRangeEnd() != null
                ? request.getRangeEnd()
                : LocalDateTime.now().plusYears(1);

        predicates.add(cb.between(root.get("eventDate"), start, end));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(root.get("eventDate"))); // или другой критерий сортировки

        return entityManager.createQuery(query)
                .setFirstResult(request.getFrom())
                .setMaxResults(request.getSize())
                .getResultList();
    }
}