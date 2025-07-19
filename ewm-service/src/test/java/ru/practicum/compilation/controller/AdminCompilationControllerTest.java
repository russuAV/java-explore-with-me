package ru.practicum.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCompilationController.class)
class AdminCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturn201AndCompilationDto() throws Exception {
        NewCompilationDto newDto = new NewCompilationDto(Set.of(1L, 2L), true,  "Подборка");
        CompilationDto savedDto = new CompilationDto(List.of(), 1L, true, "Подборка");

        Mockito.when(compilationService.create(any())).thenReturn(savedDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Подборка"));
    }

    @Test
    void create_shouldReturn400_whenTitleIsBlank() throws Exception {
        NewCompilationDto invalidDto = new NewCompilationDto(Set.of(1L), true, "");

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenTitleIsNull() throws Exception {
        String jsonWithNullTitle = "{\"pinned\":true,\"events\":[1,2],\"title\":null}";

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNullTitle))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenTitleTooLong() throws Exception {
        String longTitle = "a".repeat(51);
        NewCompilationDto invalidDto = new NewCompilationDto(Set.of(1L), true, longTitle);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200AndUpdatedCompilationDto() throws Exception {
        UpdateCompilationRequest update = new UpdateCompilationRequest(Set.of(3L), false, "Обновлено");
        CompilationDto result = new CompilationDto(List.of(), 1L, false, "Обновлено");

        Mockito.when(compilationService.update(any(), any())).thenReturn(result);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Обновлено"))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void update_shouldReturn400_whenTitleIsBlank() throws Exception {
        UpdateCompilationRequest invalid = new UpdateCompilationRequest(Set.of(1L), true, "");

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void update_shouldReturn400_whenTitleTooLong() throws Exception {
        String longTitle = "a".repeat(51);
        UpdateCompilationRequest invalid = new UpdateCompilationRequest(Set.of(1L), false, longTitle);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(compilationService).delete(1L);
    }
}