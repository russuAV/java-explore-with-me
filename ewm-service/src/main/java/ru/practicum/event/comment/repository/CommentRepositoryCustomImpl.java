package ru.practicum.event.comment.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.event.comment.model.AdminCommentSearchRequest;
import ru.practicum.event.comment.model.Comment;
import ru.practicum.event.comment.model.UserCommentSearchRequest;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {
    private final EntityManager entityManager;

    @Override
    public List<Comment> findByAdminFilter(AdminCommentSearchRequest req) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> query = cb.createQuery(Comment.class);
        Root<Comment> root = query.from(Comment.class);

        List<Predicate> predicates = new ArrayList<>();

        if (req.getEventIds() != null && !req.getEventIds().isEmpty()) {
            predicates.add(root.get("event").get("id").in(req.getEventIds()));
        }

        if (req.getAuthorIds() != null && !req.getAuthorIds().isEmpty()) {
            predicates.add(root.get("author").get("id").in(req.getAuthorIds()));
        }

        if (req.getRangeStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("created"), req.getRangeStart()));
        }

        if (req.getRangeEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("created"), req.getRangeEnd()));
        }

        query.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("created")));

        return entityManager.createQuery(query)
                .setFirstResult(req.getFrom())
                .setMaxResults(req.getSize())
                .getResultList();
    }

    @Override
    public List<Comment> findByUserFilter(Long userId, UserCommentSearchRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> query = cb.createQuery(Comment.class);
        Root<Comment> root = query.from(Comment.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("author").get("id"), userId));

        if (request.getEventIds() != null && !request.getEventIds().isEmpty()) {
            predicates.add(root.get("event").get("id").in(request.getEventIds()));
        }

        if (request.getRangeStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("created"), request.getRangeStart()));
        }
        if (request.getRangeEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("created"), request.getRangeEnd()));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(root.get("created")));

        return entityManager.createQuery(query)
                .setFirstResult(request.getFrom())
                .setMaxResults(request.getSize())
                .getResultList();
    }
}