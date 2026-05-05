package io.github.parqueubajara.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// Classe base para os testes de integração
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseController {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // JSON de registro padrão
    private final String adminJson = """
            {
                "firstName": "Admin",
                "lastName": "Teste",
                "username": "adminTeste",
                "email": "admin@teste.com",
                "password": "senha123"
            }
            """;

    protected String getAuthToken() throws Exception {
        // Registra
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminJson));

        // Faz login
        String loginJson = """
                {
                    "email": "admin@teste.com",
                    "password": "senha123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).get("token").asText();
    }
}