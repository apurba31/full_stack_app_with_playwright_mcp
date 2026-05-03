package com.fullstack.app.playwright;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/playwright")
public class PlaywrightController {

    private final PlaywrightService playwrightService;

    public PlaywrightController(PlaywrightService playwrightService) {
        this.playwrightService = playwrightService;
    }

    @PostMapping("/screenshot")
    public ResponseEntity<ScreenshotResponse> screenshot(@Valid @RequestBody ScreenshotRequest request) {
        ScreenshotResponse response = playwrightService.takeScreenshot(request);
        return ResponseEntity.ok(response);
    }
}
