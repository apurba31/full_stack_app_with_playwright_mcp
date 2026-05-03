package com.fullstack.app.playwright;

public record ScreenshotResponse(
        String url,
        String title,
        String screenshotBase64,
        int viewportWidth,
        int viewportHeight
) {
}
