package com.example.textblast;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GenerateController {

    private final ContentService service;

    public GenerateController(ContentService service) { this.service = service; }

    @PostMapping("/generate")
    public GenerateResponse generate(@Valid @RequestBody GenerateRequest request) {
        return new GenerateResponse(service.generate(request));
    }
}
