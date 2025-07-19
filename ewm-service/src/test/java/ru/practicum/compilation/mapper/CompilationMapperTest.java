package ru.practicum.compilation.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompilationMapperTest {

    private static final CompilationMapper mapper = Mappers.getMapper(CompilationMapper.class);

    @Test
    void toCompilation_shouldMapNewDto() {
        NewCompilationDto dto = new NewCompilationDto(Set.of(1L, 2L), true, "Тестовая подборка");

        Compilation result = mapper.toCompilation(dto);

        assertNotNull(result);
        assertEquals("Тестовая подборка", result.getTitle());
        assertTrue(result.getPinned());
        assertNull(result.getEvents()); // игнорируется в маппинге
    }

    @Test
    void toDto_shouldMapToDto() {
        Compilation entity = new Compilation();
        entity.setId(123L);
        entity.setTitle("Название");
        entity.setPinned(true);
        entity.setEvents(Set.of()); // пустой список, но не null

        CompilationDto dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(123L, dto.getId());
        assertEquals("Название", dto.getTitle());
        assertTrue(dto.getPinned());
        assertNotNull(dto.getEvents());
    }

    @Test
    void updateCompilationFromDto_shouldUpdateFields() {
        Compilation target = new Compilation();
        target.setTitle("Old title");
        target.setPinned(false);

        UpdateCompilationRequest update = new UpdateCompilationRequest(Set.of(10L), true, "New title");

        mapper.updateCompilationFromDto(update, target);

        assertEquals("New title", target.getTitle());
        assertTrue(target.getPinned());
        // events остаётся без изменений
    }

    @Test
    void updateCompilationFromDto_shouldIgnoreNullFields() {
        Compilation target = new Compilation();
        target.setTitle("Initial");
        target.setPinned(false);

        UpdateCompilationRequest update = new UpdateCompilationRequest(null, null, null);

        mapper.updateCompilationFromDto(update, target);

        assertEquals("Initial", target.getTitle());
        assertFalse(target.getPinned());
    }
}