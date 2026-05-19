package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.RecommendedItemRequestDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.response.RecommendedItemResponseDTO;
import io.github.parqueubajara.api.dto.update.RecommendedItemUpdateDTO;
import io.github.parqueubajara.api.mapper.RecommendedItemMapper;
import io.github.parqueubajara.api.model.RecommendedItem;
import io.github.parqueubajara.api.service.PhotoService;
import io.github.parqueubajara.api.service.RecommendedItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/recommended-items")
@RequiredArgsConstructor
public class RecommendedItemController {

    private final RecommendedItemService service;
    private final RecommendedItemMapper mapper;
    private final PhotoService photoService;

    @GetMapping
    public ResponseEntity<Page<RecommendedItemResponseDTO>> getAll(
            Pageable pageable,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean featured) {
        Page<RecommendedItemResponseDTO> page = service.findAll(pageable, active, featured)
                .map(mapper::toResponseDTO);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecommendedItemResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponseDTO(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecommendedItemResponseDTO> create(@Valid @RequestBody RecommendedItemRequestDTO dto) {
        RecommendedItem item = mapper.toEntity(dto);
        RecommendedItem saved = service.save(item);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toResponseDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody RecommendedItemUpdateDTO dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer displayOrder) throws IOException {
        PhotoResponseDTO photo = photoService.uploadForRecommendedItem(id, file, description, displayOrder);
        return ResponseEntity.ok(photo);
    }
}
