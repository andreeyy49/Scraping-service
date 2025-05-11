package webscraping.lemmasservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import webscraping.lemmasservice.service.LemmaService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lemmas")
@RequiredArgsConstructor
public class LemmaController {

    private final LemmaService lemmasService;

    @GetMapping("/search")
    public List<UUID> search(@RequestParam String query) {
        return lemmasService.findBlogIdsByRelevance(query, 10);
    }

    @GetMapping("/search/{siteId}")
    public List<UUID> search(@PathVariable("siteId") Long siteId, @RequestParam String query) {
        return lemmasService.findBlogIdsByRelevance(query, 10, siteId);
    }
}
