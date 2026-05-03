package com.fullstack.app.playwright;

import jakarta.validation.constraints.NotBlank;

public record ScreenshotRequest(
        @NotBlank(message = "url must not be blank")
        String url,

        boolean fullPage,

        String waitForSelector
) {
}
