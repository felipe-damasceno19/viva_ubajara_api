package io.github.parqueubajara.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventControllerIT extends BaseController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validEventJson = """
            {
                "name": "Festival de Inverno",
                "description": "Evento cultural de Ubajara",
                "startDateTime": "20/07/2026 19:00:00",
                "endDateTime": "25/07/2026 23:59:59",
                "location": "Centro",
                "registrationUrl": "string",
                "active": true
            }
            """;

    @Test
    @DisplayName("Deve salvar um evento e retornar 201 Created")
    void shouldSaveEvent() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEventJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar evento por ID e retornar 200 OK")
    void shouldGetEventById() throws Exception {
        String id = createEventAndGetId();

        mockMvc.perform(get("/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("Deve listar eventos com filtro de data e retornar 200 OK")
    void shouldGetAllEventsWithFilters() throws Exception {
        createEventAndGetId();

        mockMvc.perform(get("/events")
                        .param("startDateTime", "01/01/2026 00:00:00")
                        .param("endDateTime", "31/12/2026 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Deve atualizar um evento e retornar 204 No Content")
    void shouldUpdateEvent() throws Exception {
        String id = createEventAndGetId();
        String updateJson = """
                { "name": "Festival de Inverno Atualizado" }
                """;

        mockMvc.perform(put("/events/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve deletar um evento e retornar 204 No Content")
    void shouldDeleteEvent() throws Exception {
        String id = createEventAndGetId();

        mockMvc.perform(delete("/events/{id}", id))
                .andExpect(status().isNoContent());
    }

    private String createEventAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEventJson))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}