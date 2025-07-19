package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.NewCategoryDto;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с именем '" + newCategoryDto.getName() + "' уже существует");
        }

        Category category = categoryMapper.toCategory(newCategoryDto);
        Category createdCategory = categoryRepository.save(category);

        log.info("Категория добавлена: {}", createdCategory.getName());
        return categoryMapper.toCategoryDto(createdCategory);
    }

    @Override
    public void delete(Long catId) {
        Category category = getEntityById(catId);

        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictException("Существуют события, связанные с категорией");
        }

        categoryRepository.deleteById(catId);
        log.info("Категория '{}' удалена.", category.getName());
    }

    @Override
    public CategoryDto update(Long catId, NewCategoryDto newCategoryDto) {
        Category existing = getEntityById(catId);

        Optional<Category> categoryByName = categoryRepository.findByName(newCategoryDto.getName());

        if (categoryByName.isPresent() && !categoryByName.get().getId().equals(catId)) {
            throw new ConflictException("Категория с таким именем уже присутствует в базе.");
        }

        existing.setName(newCategoryDto.getName());
        Category updated = categoryRepository.save(existing);

        log.info("Данные категории '{}' изменены", updated.getName());
        return categoryMapper.toCategoryDto(updated);
    }

    @Override
    public Category getEntityById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() ->
                        new NotFoundException("Категория не найдена или недоступна"));
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = getEntityById(catId);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return categories.stream()
                .map(categoryMapper::toCategoryDto)
                .toList();
    }
}