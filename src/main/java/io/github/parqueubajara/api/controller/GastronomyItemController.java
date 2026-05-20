package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.GastronomyItemRequestDTO;
import io.github.parqueubajara.api.dto.response.GastronomyItemResponseDTO;
import io.github.parqueubajara.api.dto.update.GastronomyItemUpdateDTO;
import io.github.parqueubajara.api.model.GastronomyItem;
import io.github.parqueubajara.api.service.GastronomyItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/gastronomy-items")
@RequiredArgsConstructor
@Tag(name = "Gastronomy Items", description = "Gerenciamento dos itens gastronômicos")
public class GastronomyItemController implements GenericController {

    private final GastronomyItemService service;

    @GetMapping
    public ResponseEntity<List<GastronomyItemResponseDTO>> getActive() {
        List<GastronomyItemResponseDTO> items = service.findAllActive().stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GastronomyItemResponseDTO>> getAll() {
        List<GastronomyItemResponseDTO> items = service.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GastronomyItemResponseDTO> create(@RequestBody @Valid GastronomyItemRequestDTO dto) {
        GastronomyItem item = new GastronomyItem();
        item.setName(dto.name());
        item.setDisplayOrder(dto.displayOrder());
        item.setActive(dto.active());
        GastronomyItem saved = service.save(item);
        URI location = generateHeaderLocation(saved.getId());
        return ResponseEntity.created(location).body(toResponseDTO(saved));
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String imageUrl = service.uploadImage(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeImage(@PathVariable UUID id) {
        service.removeImage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody GastronomyItemUpdateDTO dto) {
        service.update(id, dto.name(), dto.displayOrder(), dto.active());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private GastronomyItemResponseDTO toResponseDTO(GastronomyItem item) {
        return new GastronomyItemResponseDTO(
                item.getId(),
                item.getName(),
                item.getImageUrl(),
                item.getDisplayOrder(),
                item.getActive()
        );
    }
}
