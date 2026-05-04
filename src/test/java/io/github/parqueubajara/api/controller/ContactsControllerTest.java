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
class ContactsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // TODO: Ajuste os campos do JSON de acordo com o seu ContactsRequestDTO
    private final String validContactJson = """
            {
                "name": "Secretaria de Turismo",
                "phone": "+5588999999999",
                "email": "turismo@ubajara.ce.gov.br"
            }
            """;

    private final String updateContactJson = """
            {
                "phone": "+5588988888888"
            }
            """;

    @Test
    @DisplayName("Deve salvar um contato e retornar 201 Created")
    void shouldSaveContact() throws Exception {
        mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validContactJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve buscar contato por ID e retornar 200 OK")
    void shouldGetContactById() throws Exception {
        String id = createContactAndGetId();

        mockMvc.perform(get("/contacts/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("Deve listar contatos com paginação e retornar 200 OK")
    void shouldGetAllContacts() throws Exception {
        createContactAndGetId();

        mockMvc.perform(get("/contacts")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0]").exists());
    }

    @Test
    @DisplayName("Deve atualizar um contato e retornar 204 No Content")
    void shouldUpdateContact() throws Exception {
        String id = createContactAndGetId();

        mockMvc.perform(put("/contacts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateContactJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve deletar um contato e retornar 204 No Content")
    void shouldDeleteContact() throws Exception {
        String id = createContactAndGetId();

        mockMvc.perform(delete("/contacts/{id}", id))
                .andExpect(status().isNoContent());
    }

    private String createContactAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validContactJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}