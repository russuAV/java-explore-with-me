package ru.practicum.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.NewCategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.exception.ConflictException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCategoryController.class)
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCategory() throws Exception {
        NewCategoryDto newCategory = new NewCategoryDto("Спорт");
        CategoryDto result = new CategoryDto(1L, "Спорт");

        Mockito.when(categoryService.create(any(NewCategoryDto.class))).thenReturn(result);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(result.getId()))
                .andExpect(jsonPath("$.name").value(result.getName()));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        Long catId = 1L;
        NewCategoryDto updateDto = new NewCategoryDto("Музыка");
        CategoryDto updated = new CategoryDto(catId, "Музыка");

        Mockito.when(categoryService.update(eq(catId), any(NewCategoryDto.class))).thenReturn(updated);

        mockMvc.perform(patch("/admin/categories/{catId}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catId))
                .andExpect(jsonPath("$.name").value("Музыка"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        Long catId = 1L;

        mockMvc.perform(delete("/admin/categories/{catId}", catId))
                .andExpect(status().isNoContent());

        Mockito.verify(categoryService).delete(catId);
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        NewCategoryDto newCategory = new NewCategoryDto(" ");  // пустая строка

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateWithBlankName() throws Exception {
        NewCategoryDto updateDto = new NewCategoryDto("");  // пустое имя

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictIfNameAlreadyExists() throws Exception {
        NewCategoryDto newCategory = new NewCategoryDto("Кино");

        Mockito.when(categoryService.create(any(NewCategoryDto.class)))
                .thenThrow(new ConflictException("Категория с таким именем уже существует"));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Категория с таким именем уже существует"));
    }
}