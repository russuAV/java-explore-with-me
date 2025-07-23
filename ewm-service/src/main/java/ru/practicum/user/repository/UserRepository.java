package ru.practicum.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIdIn(List<Long> ids, Pageable pageable);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE (:ids IS NULL OR u.id IN :ids) ORDER BY u.id LIMIT :size OFFSET :from")
    List<User> findUsersWithOffset(
            @Param("ids") List<Long> ids,
            @Param("from") int from,
            @Param("size") int size
    );
}