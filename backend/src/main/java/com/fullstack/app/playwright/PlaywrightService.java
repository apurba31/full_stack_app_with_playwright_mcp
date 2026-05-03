package com.fullstack.app.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class PlaywrightService {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightService.class);
    private static final int DEFAULT_VIEWPORT_WIDTH = 1280;
    private static final int DEFAULT_VIEWPORT_HEIGHT = 720;

    public ScreenshotResponse takeScreenshot(ScreenshotRequest request) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
            try {
                Page page = browser.newPage();
                page.setViewportSize(DEFAULT_VIEWPORT_WIDTH, DEFAULT_VIEWPORT_HEIGHT);
                log.debug("Navigating to {}", request.url());
                page.navigate(request.url());

                if (request.waitForSelector() != null && !request.waitForSelector().isBlank()) {
                    page.waitForSelector(request.waitForSelector());
                }

                String title = page.title();
                byte[] screenshotBytes = page.screenshot(
                        new Page.ScreenshotOptions().setFullPage(request.fullPage())
                );
                String base64 = Base64.getEncoder().encodeToString(screenshotBytes);
                Page.ViewportSize viewport = page.viewportSize();
                int width = viewport != null ? viewport.width : DEFAULT_VIEWPORT_WIDTH;
                int height = viewport != null ? viewport.height : DEFAULT_VIEWPORT_HEIGHT;

                return new ScreenshotResponse(request.url(), title, base64, width, height);
            } finally {
                browser.close();
            }
        }
    }
}
