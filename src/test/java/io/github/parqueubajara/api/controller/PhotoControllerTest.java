package io.github.parqueubajara.api.controller;

import jakarta.servlet.http.Part;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhotoControllerIT extends BaseController{

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve realizar upload de foto e retornar 201 Created")
    void shouldUploadPhoto() throws Exception {
        String token = getAuthToken();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo da imagem".getBytes()
        );

        mockMvc.perform(multipart("/photos")
                        .file(file)
                        .param("description", "Foto da trilha")
                        .param("displayOrder", "1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar deletar foto inexistente")
    void shouldReturn404OnDeleteInvalidPhoto() throws Exception {
        mockMvc.perform(delete("/photos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}