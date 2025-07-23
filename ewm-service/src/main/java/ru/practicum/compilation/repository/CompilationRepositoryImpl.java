package ru.practicum.compilation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompilationRepositoryImpl implements CompilationRepositoryCustom {
    private final EntityManager entityManager;

    @Override
    public List<Compilation> findCompilations(Boolean pinned, int from, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Compilation> query = cb.createQuery(Compilation.class);
        Root<Compilation> root = query.from(Compilation.class);

        if (pinned != null) {
            query.where(cb.equal(root.get("pinned"), pinned));
        }

        query.orderBy(cb.desc(root.get("id")));

        return entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }
}