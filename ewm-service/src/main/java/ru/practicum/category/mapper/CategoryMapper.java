package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.NewCategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toCategoryDto(Category category);

    Category toCategory(NewCategoryDto newCategoryDto);
}
