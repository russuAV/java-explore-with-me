package ru.practicum.compilation.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CompilationRepositoryTest {

    @Autowired
    private CompilationRepository compilationRepository;

    @Test
    void findAllByPinned_shouldReturnPinnedCompilations() {
        Compilation pinned1 = compilationRepository.save(
                new Compilation(null, "Закреп 1", true, null));
        Compilation pinned2 = compilationRepository.save(
                new Compilation(null, "Закреп 2", true, null));
        Compilation unpinned = compilationRepository.save(
                new Compilation(null, "Обычная", false, null));

        List<Compilation> pinnedResult = compilationRepository.findAllByPinned(
                true, PageRequest.of(0, 10));
        List<Compilation> unpinnedResult = compilationRepository.findAllByPinned(
                false, PageRequest.of(0, 10));

        assertThat(pinnedResult)
                .hasSize(2)
                .extracting(Compilation::getTitle)
                .containsExactlyInAnyOrder("Закреп 1", "Закреп 2");

        assertThat(unpinnedResult)
                .hasSize(1)
                .extracting(Compilation::getTitle)
                .containsExactly("Обычная");
    }
}