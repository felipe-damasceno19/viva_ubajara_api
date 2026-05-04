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
class AirportControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // TODO: Ajuste os campos do JSON de acordo com o seu AirportRequestDTO
    private final String validAirportJson = """
            {
                "name": "Aeroporto de Jericoacoara",
                "city": "Cruz",
                "iataCode": "JJD"
            }
            """;

    private final String updateAirportJson = """
            {
                "name": "Aeroporto de Jericoacoara Atualizado"
            }
            """;

    @Test
    @DisplayName("Deve salvar um aeroporto e retornar 201 Created")
    void shouldSaveAirport() throws Exception {
        mockMvc.perform(post("/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAirportJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar aeroporto por ID e retornar 200 OK")
    void shouldGetAirportById() throws Exception {
        String id = createAirportAndGetId();

        mockMvc.perform(get("/airports/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("Deve listar aeroportos com paginação e retornar 200 OK")
    void shouldGetAllAirports() throws Exception {
        createAirportAndGetId();

        mockMvc.perform(get("/airports")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0]").exists());
    }

    @Test
    @DisplayName("Deve atualizar um aeroporto e retornar 204 No Content")
    void shouldUpdateAirport() throws Exception {
        String id = createAirportAndGetId();

        mockMvc.perform(put("/airports/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAirportJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve deletar um aeroporto e retornar 204 No Content")
    void shouldDeleteAirport() throws Exception {
        String id = createAirportAndGetId();

        mockMvc.perform(delete("/airports/{id}", id))
                .andExpect(status().isNoContent());
    }

    // Método auxiliar para criar um registro e extrair o ID para usar nos testes de GET, PUT e DELETE
    private String createAirportAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAirportJson))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("id").asText();
    }
}