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
class RestaurantControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validRestaurantJson = """
            {
                "name": "Restaurante Mirante",
                "description": "Comida regional com vista",
                "address": "Estrada do Teleferico, s/n"
            }
            """;

    @Test
    @DisplayName("Deve salvar um restaurante e retornar 201 Created")
    void shouldSaveRestaurant() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRestaurantJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Restaurante Mirante"));
    }

    @Test
    @DisplayName("Deve buscar todos os restaurantes paginados")
    void shouldGetAllRestaurants() throws Exception {
        createRestaurantAndGetId();

        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    @Test
    @DisplayName("Deve atualizar restaurante e retornar 204")
    void shouldUpdateRestaurant() throws Exception {
        String id = createRestaurantAndGetId();
        String updateJson = """
                { "name": "Mirante Gourmet" }
                """;

        mockMvc.perform(put("/restaurants/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());
    }

    private String createRestaurantAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRestaurantJson))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}