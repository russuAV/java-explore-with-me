package ru.practicum.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.exception.NotFoundException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PublicCategoryController.class)
class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCategories_shouldReturnList() throws Exception {
        CategoryDto dto1 = new CategoryDto(1L, "Books");
        CategoryDto dto2 = new CategoryDto(2L, "Games");

        when(categoryService.getCategories(0, 10))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Books")));
    }


    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {
        CategoryDto dto = new CategoryDto(1L, "Books");

        when(categoryService.getCategoryById(1L))
                .thenReturn(dto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Books")));
    }

    @Test
    void getCategoryById_notFound_shouldReturn404() throws Exception {
        when(categoryService.getCategoryById(999L))
                .thenThrow(new NotFoundException("Категория не найдена"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }
}