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
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Usando o endpoint de registro para popular o banco para os testes de visualização
    private final String registerUserJson = """
            {
                "name": "Admin Teste",
                "email": "admin@parque.com",
                "password": "senhaSegura123"
            }
            """;

    @Test
    @DisplayName("Deve listar usuários com filtro opcional de username")
    void shouldFindAllUsers() throws Exception {
        setupUser();

        mockMvc.perform(get("/users")
                        .param("username", "admin")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID e retornar 200 OK")
    void shouldGetUserById() throws Exception {
        String id = setupUser();

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("Deve atualizar parcialmente um usuário via Patch e retornar 204")
    void shouldPatchUser() throws Exception {
        String id = setupUser();
        String patchJson = """
                { "name": "Nome Alterado" }
                """;

        mockMvc.perform(patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve deletar usuário e retornar 204 No Content")
    void shouldDeleteUser() throws Exception {
        String id = setupUser();

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    /**
     * Como o UserController não tem método de criação (usado o AuthController),
     * criamos o usuário via /auth/register para ter dados reais no banco.
     */
    private String setupUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserJson))
                .andReturn();

        // Assume que o AuthResponseDTO retorna o ID ou pegamos do banco se necessário.
        // Se o AuthResponse não retornar ID, use a lógica de busca por email aqui.
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("userId").asText();
    }
}