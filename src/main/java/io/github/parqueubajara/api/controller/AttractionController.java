package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.AttractionRequestDTO;
import io.github.parqueubajara.api.dto.response.AttractionResponseDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.update.AttractionUpdateDTO;
import io.github.parqueubajara.api.mapper.AttractionMapper;
import io.github.parqueubajara.api.model.Attraction;
import io.github.parqueubajara.api.model.enums.AttractionType;
import io.github.parqueubajara.api.service.AttractionService;
import io.github.parqueubajara.api.service.PhotoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/attractions")
@RequiredArgsConstructor
@Tag(name = "Attractions", description = "Gerenciamento dos atrativos turísticos")
public class AttractionController implements GenericController{

    private final AttractionService service;
    private final AttractionMapper mapper;
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<AttractionResponseDTO> getById(@PathVariable UUID id){
        Attraction attraction = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(attraction));
    }

    @GetMapping()
    public ResponseEntity<Page<AttractionResponseDTO>> getAll(
            @RequestParam(required = false) AttractionType category,
            @PageableDefault(size = 10, sort = "name")Pageable pageable){

        Page<Attraction> pageEntity = service.findAll(pageable, category);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttractionResponseDTO> save(@RequestBody @Valid AttractionRequestDTO requestDTO){
        Attraction attraction = mapper.toEntity(requestDTO);
        service.save(attraction);
        URI location = generateHeaderLocation(attraction.getId());

        return ResponseEntity.created(location).body(mapper.toResponseDTO(attraction));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttractionResponseDTO> addSubAttraction(
            @PathVariable UUID id,
            @RequestBody AttractionRequestDTO requestDTO) {
        Attraction attraction = mapper.toEntity(requestDTO);
        service.linkAttractions(id, attraction);
        URI location = generateHeaderLocation(id);

        return ResponseEntity.ok(mapper.toResponseDTO(attraction));
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
    ) throws IOException {
        PhotoResponseDTO response = photoService.uploadForAttraction(
                id, file, description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody AttractionUpdateDTO updateDTO){
        Attraction attraction = service.findById(id);
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
