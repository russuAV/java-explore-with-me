package ru.practicum.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCompilationController.class)
class PublicCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnListOfCompilations() throws Exception {
        CompilationDto dto1 = new CompilationDto(List.of(), 1L, true, "Топ-1");
        CompilationDto dto2 = new CompilationDto(List.of(), 2L, false,  "Топ-2");

        Mockito.when(compilationService.getAllCompilations(any(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Топ-1"))
                .andExpect(jsonPath("$[1].title").value("Топ-2"));
    }

    @Test
    void getById_shouldReturnCompilationById() throws Exception {
        CompilationDto dto = new CompilationDto(List.of(), 42L, true, "Пенная вечеринка");

        Mockito.when(compilationService.getCompilationById(eq(42L), any(HttpServletRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(get("/compilations/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42L))
                .andExpect(jsonPath("$.title").value("Пенная вечеринка"))
                .andExpect(jsonPath("$.pinned").value(true));
    }
}