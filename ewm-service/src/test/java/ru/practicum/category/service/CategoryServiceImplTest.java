package ru.practicum.category.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.NewCategoryDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private final NewCategoryDto newCategoryDto = new NewCategoryDto("Концерты");
    private final Category category = new Category(1L, "Концерты");
    private final CategoryDto categoryDto = new CategoryDto(1L, "Концерты");

    @Test
    void create_whenNameUnique_shouldCreateCategory() {
        when(categoryRepository.existsByName(newCategoryDto.getName())).thenReturn(false);
        when(categoryMapper.toCategory(newCategoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toCategoryDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.create(newCategoryDto);

        assertEquals(categoryDto, result);
        verify(categoryRepository).save(category);
    }

    @Test
    void create_whenNameExists_shouldThrowConflictException() {
        when(categoryRepository.existsByName(newCategoryDto.getName())).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.create(newCategoryDto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_whenNoEvents_shouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_whenCategoryNotFound_shouldThrowNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.delete(1L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void delete_whenEventsExist_shouldThrowConflictException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.delete(1L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void update_whenValidData_shouldUpdateCategory() {
        NewCategoryDto updateDto = new NewCategoryDto("Кино");
        Category updatedCategory = new Category(1L, "Кино");
        CategoryDto updatedDto = new CategoryDto(1L, "Кино");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Кино")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenReturn(updatedCategory);
        when(categoryMapper.toCategoryDto(updatedCategory)).thenReturn(updatedDto);

        CategoryDto result = categoryService.update(1L, updateDto);

        assertEquals(updatedDto, result);
        verify(categoryRepository).save(category);
    }

    @Test
    void update_whenNameTakenByOtherCategory_shouldThrowConflictException() {
        NewCategoryDto updateDto = new NewCategoryDto("Кино");
        Category otherCategory = new Category(2L, "Кино");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Кино")).thenReturn(Optional.of(otherCategory));

        assertThrows(ConflictException.class, () -> categoryService.update(1L, updateDto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_whenSameName_shouldUpdateSuccessfully() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Концерты")).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toCategoryDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.update(1L, newCategoryDto);

        assertEquals(categoryDto, result);
        verify(categoryRepository).save(category);
    }

    @Test
    void getEntityById_whenExists_shouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getEntityById(1L);

        assertEquals(category, result);
    }

    @Test
    void getEntityById_whenNotExists_shouldThrowNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getEntityById(1L));
    }

    @Test
    void getCategoryById_whenExists_shouldReturnDto() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toCategoryDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.getCategoryById(1L);

        assertEquals(categoryDto, result);
    }

    @Test
    void getCategories_shouldReturnPaginatedResults() {
        int from = 0;
        int size = 10;
        List<Category> categories = List.of(category);

        when(categoryRepository.findWithOffset(from, size)).thenReturn(categories);
        when(categoryMapper.toCategoryDto(category)).thenReturn(categoryDto);

        List<CategoryDto> result = categoryService.getCategories(from, size);

        assertEquals(1, result.size());
        assertEquals(categoryDto, result.get(0));

        verify(categoryRepository).findWithOffset(from, size);
        verify(categoryMapper).toCategoryDto(category);
    }

    @Test
    void getCategories_whenEmptyResult_shouldReturnEmptyList() {
        when(categoryRepository.findWithOffset(0, 10)).thenReturn(List.of());

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCategories_whenCalled_shouldUseCorrectOffsetAndLimit() {
        int from = 10;
        int size = 5;

        when(categoryRepository.findWithOffset(from, size)).thenReturn(List.of());
        categoryService.getCategories(from, size);

        verify(categoryRepository).findWithOffset(from, size);
    }
}