package io.github.parqueubajara.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.parqueubajara.api.model.BaseEntity;
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
class TourGuideControllerIT extends BaseController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validGuideJson = """
            {
              "name": "Marcio Costa",
              "phone": "(88)9887-9742",
              "email": "marciocost@gmail.com",
              "languages": [
                "Portugues",
                "Ingles"
              ],
              "description": "Muito simpático",
              "active": true
            }
            """;

    @Test
    @DisplayName("Deve salvar um guia turístico e retornar 201 Created")
    void shouldSaveTourGuide() throws Exception {
        mockMvc.perform(post("/tour-guides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validGuideJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar guia por ID e retornar 200 OK")
    void shouldGetGuideById() throws Exception {
        String id = createGuideAndGetId();

        mockMvc.perform(get("/tour-guides/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("João Guia Ubajara"));
    }

    @Test
    @DisplayName("Deve retornar página de guias e retornar 200 OK")
    void shouldGetAllGuides() throws Exception {
        createGuideAndGetId();

        mockMvc.perform(get("/tour-guides")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Deve atualizar guia e retornar 204 No Content")
    void shouldUpdateGuide() throws Exception {
        String id = createGuideAndGetId();
        String updateJson = """
                { "name": "João Guia Atualizado" }
                """;

        mockMvc.perform(put("/tour-guides/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve deletar guia e retornar 204 No Content")
    void shouldDeleteGuide() throws Exception {
        String id = createGuideAndGetId();

        mockMvc.perform(delete("/tour-guides/{id}", id))
                .andExpect(status().isNoContent());
    }

    private String createGuideAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/tour-guides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validGuideJson))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}