package ru.practicum.category.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.category.model.Category;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void existsByName_shouldReturnTrue() {
        Category category = new Category();
        category.setName("TestName");
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsByName("TestName");

        assertThat(exists).isTrue();
    }

    @Test
    void findByName_shouldReturnCategory() {
        Category category = new Category();
        category.setName("Books");
        categoryRepository.save(category);

        var found = categoryRepository.findByName("Books");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Books");
    }
}