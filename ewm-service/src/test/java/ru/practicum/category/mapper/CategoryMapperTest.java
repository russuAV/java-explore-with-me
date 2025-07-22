package ru.practicum.category.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.NewCategoryDto;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toCategoryDto_shouldMapCorrectly() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Music");

        CategoryDto dto = mapper.toCategoryDto(category);

        assertEquals(1L, dto.getId());
        assertEquals("Music", dto.getName());
    }

    @Test
    void toCategory_shouldMapFromNewDto() {
        NewCategoryDto newDto = new NewCategoryDto();
        newDto.setName("Books");

        Category category = mapper.toCategory(newDto);

        assertEquals("Books", category.getName());
    }
}