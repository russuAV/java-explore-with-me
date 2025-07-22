package ru.practicum.compilation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    CompilationRepository compilationRepository;

    @Mock
    CompilationMapper compilationMapper;

    @Mock
    EventService eventService;

    @InjectMocks
    CompilationServiceImpl compilationService;

    @Test
    void create_shouldReturnSavedDto() {
        NewCompilationDto newDto = new NewCompilationDto(Set.of(1L, 2L), true, "Топовое");
        Compilation entity = new Compilation();
        Compilation saved = new Compilation();
        CompilationDto expectedDto = new CompilationDto();

        Set<Event> events = Set.of(mock(Event.class), mock(Event.class));

        when(compilationMapper.toCompilation(newDto)).thenReturn(entity);
        when(eventService.findAllById(Set.of(1L, 2L))).thenReturn(events);
        when(compilationRepository.save(entity)).thenReturn(saved);
        when(compilationMapper.toDto(saved)).thenReturn(expectedDto);

        CompilationDto result = compilationService.create(newDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(compilationRepository).save(entity);
    }

    @Test
    void update_shouldApplyChangesAndReturnDto() {
        Long compId = 1L;
        UpdateCompilationRequest updateDto = new UpdateCompilationRequest(Set.of(5L), false, "Новое");
        Compilation existing = new Compilation();
        CompilationDto dto = new CompilationDto();
        Set<Event> events = Set.of(new Event());

        when(compilationRepository.findById(compId)).thenReturn(Optional.of(existing));
        when(eventService.findAllById(Set.of(5L))).thenReturn(events);
        when(compilationRepository.save(existing)).thenReturn(existing);
        when(compilationMapper.toDto(existing)).thenReturn(dto);

        CompilationDto result = compilationService.update(compId, updateDto);

        assertThat(result).isEqualTo(dto);
        verify(compilationMapper).updateCompilationFromDto(updateDto, existing);
    }

    @Test
    void delete_shouldRemoveCompilation() {
        Compilation existing = new Compilation();
        when(compilationRepository.findById(42L)).thenReturn(Optional.of(existing));

        compilationService.delete(42L);

        verify(compilationRepository).delete(existing);
    }

    @Test
    void getCompilationById_shouldReturnDto() {
        Compilation comp = new Compilation();
        CompilationDto dto = new CompilationDto();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(comp));
        when(compilationMapper.toDto(comp)).thenReturn(dto);

        CompilationDto result = compilationService.getCompilationById(1L, null);

        assertThat(result).isEqualTo(dto);
    }
}