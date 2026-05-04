package io.github.parqueubajara.api.controller;

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
class TouristSpotControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validSpotJson = """
            {
                "name": "Bondinho de Ubajara",
                "description": "Famoso teleférico com vista panorâmica"
            }
            """;

    @Test
    @DisplayName("Deve salvar um ponto turístico e retornar 201 Created")
    void shouldSaveTouristSpot() throws Exception {
        mockMvc.perform(post("/tourist-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSpotJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar ponto turístico por ID e retornar 200 OK")
    void shouldGetSpotById() throws Exception {
        String id = createSpotAndGetId();

        mockMvc.perform(get("/tourist-spots/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("Deve listar pontos turísticos ordenados por nome")
    void shouldGetAllSpots() throws Exception {
        createSpotAndGetId();

        mockMvc.perform(get("/tourist-spots")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    @Test
    @DisplayName("Deve atualizar ponto turístico e retornar 204 No Content")
    void shouldUpdateSpot() throws Exception {
        String id = createSpotAndGetId();
        String updateJson = """
                { "description": "Nova descrição atualizada" }
                """;

        mockMvc.perform(put("/tourist-spots/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve deletar ponto turístico e retornar 204 No Content")
    void shouldDeleteSpot() throws Exception {
        String id = createSpotAndGetId();

        mockMvc.perform(delete("/tourist-spots/{id}", id))
                .andExpect(status().isNoContent());
    }

    private String createSpotAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/tourist-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSpotJson))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}