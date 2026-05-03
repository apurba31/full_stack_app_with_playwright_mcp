package com.fullstack.app.playwright;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaywrightController.class)
class PlaywrightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaywrightService playwrightService;

    @Test
    void screenshotReturns200WithResponse() throws Exception {
        ScreenshotResponse response = new ScreenshotResponse(
                "https://example.com", "Example Domain", "base64data==", 1280, 720
        );
        when(playwrightService.takeScreenshot(any(ScreenshotRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/playwright/screenshot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ScreenshotRequest("https://example.com", false, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.com"))
                .andExpect(jsonPath("$.title").value("Example Domain"))
                .andExpect(jsonPath("$.screenshotBase64").value("base64data=="))
                .andExpect(jsonPath("$.viewportWidth").value(1280))
                .andExpect(jsonPath("$.viewportHeight").value(720));
    }

    @Test
    void screenshotReturns400WhenUrlBlank() throws Exception {
        mockMvc.perform(post("/api/v1/playwright/screenshot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\",\"fullPage\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("url"));
    }

    @Test
    void screenshotReturns400WhenBodyMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/playwright/screenshot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void screenshotWithFullPageAndSelector() throws Exception {
        ScreenshotResponse response = new ScreenshotResponse(
                "https://example.com", "Page", "abc123", 1280, 720
        );
        when(playwrightService.takeScreenshot(any(ScreenshotRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/playwright/screenshot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ScreenshotRequest("https://example.com", true, ".main-content"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.screenshotBase64").value("abc123"));
    }
}
