package ru.practicum.stats.hit;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;

@RestController
@RequestMapping("/hit")
@RequiredArgsConstructor
public class HitController {

    private final HitService hitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody EndpointHitDto hitDto) {
        hitService.save(hitDto);
    }
}