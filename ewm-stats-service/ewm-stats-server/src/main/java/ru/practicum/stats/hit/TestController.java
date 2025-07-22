package ru.practicum.stats.hit;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final HitRepository hitRepository;

    @DeleteMapping("/reset-hits")
    public void reset() {
        hitRepository.deleteAll();
    }
}