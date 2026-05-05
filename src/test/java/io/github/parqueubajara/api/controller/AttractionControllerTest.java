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
class AttractionControllerIT extends BaseController{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // TODO: Ajuste os campos do JSON de acordo com o seu AttractionRequestDTO e AttractionType
    private final String validAttractionJson = """
            {
              "name": "Gruta",
              "description": "do ubajara",
              "address": "parque nacional",
              "phone": "8898977-4241",
              "email": "gruta@gmail.com",
              "webUrl": "gruta.com.br",
              "instagramUrl": "@gruta",
              "active": true,
              "openingHours": "06:00",
              "entryPrice": 0,
              "hasGuide": true,
              "averageVisitDuration": 50,
              "category": "PARK"
            }
            """;

    private final String updateAttractionJson = """
            {
              "name": "Gruta do ubajara",
              "description": "ubajara",
              "address": "parque ecologico do ubajara",
              "phone": "8898977-4241",
              "email": "gruta@gmail.com",
              "webUrl": "gruta.com.br",
              "instagramUrl": "@gruta",
              "active": true,
              "openingHours": "06:00",
              "entryPrice": 0,
              "hasGuide": true,
              "averageVisitDuration": 50,
              "category": "PARK"
            }
            """;

    @Test
    @DisplayName("Deve salvar uma atração e retornar 201 Created")
    void shouldSaveAttraction() throws Exception {
        mockMvc.perform(post("/attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAttractionJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar atração por ID e retornar 200 OK")
    void shouldGetAttractionById() throws Exception {
        String id = createAttractionAndGetId();

        mockMvc.perform(get("/attractions/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("Deve listar atrações com filtro de categoria e paginação (200 OK)")
    void shouldGetAllAttractionsWithFilter() throws Exception {
        createAttractionAndGetId();

        // O parâmetro category deve bater com a String gerada no Json/Enum
        mockMvc.perform(get("/attractions")
                        .param("category", "NATUREZA")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0]").exists());
    }

    @Test
    @DisplayName("Deve atualizar uma atração e retornar 204 No Content")
    void shouldUpdateAttraction() throws Exception {
        String id = createAttractionAndGetId();

        mockMvc.perform(put("/attractions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAttractionJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve deletar uma atração e retornar 204 No Content")
    void shouldDeleteAttraction() throws Exception {
        String id = createAttractionAndGetId();

        mockMvc.perform(delete("/attractions/{id}", id))
                .andExpect(status().isNoContent());
    }

    private String createAttractionAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAttractionJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}