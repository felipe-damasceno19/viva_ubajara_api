package io.github.parqueubajara.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    // TODO: Ajuste de acordo com UserRequestDTO
    private final String registerJson = """
            {
                "name": "Turista Silva",
                "email": "turista@email.com",
                "password": "senhaForte123"
            }
            """;

    // TODO: Ajuste de acordo com LoginRequestDTO
    private final String loginJson = """
            {
                "email": "turista@email.com",
                "password": "senhaForte123"
            }
            """;

    @Test
    @DisplayName("Deve registrar um usuário com sucesso e retornar 201 Created")
    void shouldRegisterUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists()); // Verifica se o DTO de reposta tem o token/id
    }

    @Test
    @DisplayName("Deve falhar ao registrar usuário com email já existente (400 Bad Request)")
    void shouldFailRegisterWithExistingEmail() throws Exception {
        // Registra a primeira vez
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        // Tenta registrar novamente com o mesmo payload
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar 200 OK")
    void shouldLoginSuccessfully() throws Exception {
        // Registra o usuário antes de testar o login
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        // Realiza o login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized para login inválido")
    void shouldReturn401ForInvalidLogin() throws Exception {
        String invalidLoginJson = """
            {
                "email": "inexistente@email.com",
                "password": "senhaerrada"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginJson))
                .andExpect(status().isUnauthorized()); // Considerando que sua service lance exceção resultando em 401
    }
}