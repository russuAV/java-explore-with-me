package ru.practicum.stats.hit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@DataJpaTest
class HitRepositoryTest {

    @Autowired
    private HitRepository hitRepository;

    @Test
    void saveAndFindById_ShouldPersistAllFields() {
        LocalDateTime now = LocalDateTime.now();
        EndpointHit hit = new EndpointHit(
                null,
                "my-app",
                "/test/uri",
                "123.45.67.89",
                now
        );

        EndpointHit saved = hitRepository.save(hit);
        Long id = saved.getId();
        assertThat(id).isNotNull();

        Optional<EndpointHit> fetched = hitRepository.findById(id);
        assertThat(fetched).isPresent();
        EndpointHit h = fetched.get();
        assertThat(h.getApp()).isEqualTo("my-app");
        assertThat(h.getUri()).isEqualTo("/test/uri");
        assertThat(h.getIp()).isEqualTo("123.45.67.89");
        assertThat(h.getTimestamp()).isEqualTo(now);
    }

    @Test
    void findAll_ShouldReturnAllSavedHits() {
        EndpointHit h1 = new EndpointHit(
                null, "app1", "/a", "1.1.1.1", LocalDateTime.now());
        EndpointHit h2 = new EndpointHit(
                null, "app2", "/b", "2.2.2.2", LocalDateTime.now().plusMinutes(1));
        hitRepository.saveAll(List.of(h1, h2));

        List<EndpointHit> all = hitRepository.findAll();
        assertThat(all).hasSize(2)
                .extracting(EndpointHit::getApp, EndpointHit::getUri)
                .containsExactlyInAnyOrder(
                        tuple("app1", "/a"),
                        tuple("app2", "/b")
                );
    }
}