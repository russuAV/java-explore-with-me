package ru.practicum.category.service;

import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    void delete(Long catId);

    CategoryDto update(Long catId, NewCategoryDto newCategoryDto);

    Category getEntityById(Long catId);

    CategoryDto getCategoryById(Long catId);

    List<CategoryDto> getCategories(int from, int size);
}