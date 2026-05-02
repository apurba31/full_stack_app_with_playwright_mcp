package com.fullstack.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.domain.TaskStatus;
import com.fullstack.app.dto.CreateTaskRequest;
import com.fullstack.app.dto.TaskDto;
import com.fullstack.app.dto.UpdateStatusRequest;
import com.fullstack.app.dto.UpdateTaskRequest;
import com.fullstack.app.exception.TaskNotFoundException;
import com.fullstack.app.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService service;

    private TaskDto sample(UUID id) {
        Instant now = Instant.now();
        return new TaskDto(id, "Title", "Desc", TaskStatus.TODO, TaskPriority.MEDIUM, now, now);
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        TaskDto dto = sample(id);
        when(service.create(any(CreateTaskRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTaskRequest("Title", "Desc", TaskPriority.MEDIUM))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tasks/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void createReturns400WhenTitleBlank() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"D\",\"priority\":\"LOW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    void createReturns400WhenJsonMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void getByIdReturns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenReturn(sample(id));
        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new TaskNotFoundException(id));
        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getByIdReturns400WhenIdInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        Page<TaskDto> page = new PageImpl<>(List.of(sample(id)));
        when(service.list(eq(TaskStatus.TODO), eq(TaskPriority.MEDIUM), eq("title"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks")
                        .param("status", "TODO")
                        .param("priority", "MEDIUM")
                        .param("q", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void updateReturns200() throws Exception {
        UUID id = UUID.randomUUID();
        TaskDto dto = sample(id);
        when(service.update(eq(id), any(UpdateTaskRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTaskRequest("New", "Desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void updateReturns400WhenInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"D\",\"status\":\"TODO\",\"priority\":\"LOW\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchStatusReturns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.patchStatus(eq(id), any(UpdateStatusRequest.class))).thenReturn(sample(id));

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void patchStatusReturns400WhenStatusMissing() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReturns204() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/tasks/{id}", id))
                .andExpect(status().isNoContent());
        verify(service).delete(id);
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new TaskNotFoundException(id)).when(service).delete(id);
        mockMvc.perform(delete("/api/v1/tasks/{id}", id))
                .andExpect(status().isNotFound());
    }
}
