package ru.practicum.stats.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
@ContextConfiguration(classes = {StatsController.class, StatsServiceImpl.class})
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testGetStats() throws Exception {
        List<ViewStatsDto> mockResult = List.of(
                new ViewStatsDto("main-service", "/event", 5L),
                new ViewStatsDto("main-service", "/user", 2L)
        );

        Mockito.when(statsService.getStats(any(), any(), any(), eq(false)))
                .thenReturn(mockResult);

        String start = LocalDateTime.now().minusDays(1).format(formatter);
        String end = LocalDateTime.now().format(formatter);

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("uris", "/event", "/user")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].app").value("main-service"))
                .andExpect(jsonPath("$[0].uri").value("/event"))
                .andExpect(jsonPath("$[0].hits").value(5));
    }
}