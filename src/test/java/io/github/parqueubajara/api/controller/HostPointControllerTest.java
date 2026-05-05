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
class HostPointControllerIT extends BaseController{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validHostJson = """
            {
              "name": "Pousada do sereno",
              "description": "pousada",
              "address": "Centro",
              "phone": "(88)99662-8976",
              "email": "pousada@gmail.com",
              "webUrl": "pousadadosereno.com",
              "instagramUrl": "@pousadaS",
              "active": true,
              "hostType": "HOTEL",
              "numOfRooms": 10,
              "avgPrice": 80,
              "bookingUrl": "string"
            }
            """;

    @Test
    @DisplayName("Deve salvar uma hospedagem e retornar 201 Created")
    void shouldSaveHostPoint() throws Exception {
        mockMvc.perform(post("/host-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validHostJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve filtrar hospedagens por tipo e retornar 200 OK")
    void shouldGetAllByFilter() throws Exception {
        createHostAndGetId();

        mockMvc.perform(get("/host-points")
                        .param("type", "HOTEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("HOTEL"));
    }

    @Test
    @DisplayName("Deve retornar 204 ao deletar hospedagem")
    void shouldDeleteHost() throws Exception {
        String id = createHostAndGetId();
        mockMvc.perform(delete("/host-points/{id}", id))
                .andExpect(status().isNoContent());
    }

    private String createHostAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/host-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validHostJson))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}