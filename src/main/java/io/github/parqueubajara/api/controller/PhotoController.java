package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.handler.StandardError;
import io.github.parqueubajara.api.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
@Tag(name = "Photos", description = "Upload e remoção de fotos via AWS S3")
public class PhotoController {

    private final PhotoService service;

    @Operation(
            summary = "Upload de foto",
            description = "Envia imagem para o AWS S3 e salva a URL no banco. " +
                    "Aceita JPEG, PNG e WEBP com tamanho máximo de 5MB"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto enviada com sucesso",
                    content = @Content(schema = @Schema(implementation = PhotoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhotoResponseDTO> upload(
            @RequestPart("file")MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
            ) throws IOException{

        PhotoResponseDTO responseDTO = service.upload(
                file,
                description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Deletar foto", description = "Remove a foto do S3 e do banco pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


