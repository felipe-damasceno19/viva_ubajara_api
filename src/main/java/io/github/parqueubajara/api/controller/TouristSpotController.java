package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.TouristSpotRequestDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.response.TouristSpotResponseDTO;
import io.github.parqueubajara.api.dto.update.TouristSpotUpdateDTO;
import io.github.parqueubajara.api.mapper.TouristSpotMapper;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.service.PhotoService;
import io.github.parqueubajara.api.service.TouristSpotService;
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
import java.util.UUID;

@RestController
@RequestMapping("/tourist-spots")
@RequiredArgsConstructor
@Tag(name = "Tourist Spots", description = "Gerenciamento dos pontos turísticos")
public class TouristSpotController {

    private final TouristSpotService service;
    private final TouristSpotMapper mapper;
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<TouristSpotResponseDTO> getById(@PathVariable UUID id){
        TouristSpot spot = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(spot));
    }

    @GetMapping
    public ResponseEntity<Page<TouristSpotResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            @RequestParam(required = false) Boolean active) {
        Page<TouristSpot> pageEntity = service.findAll(pageable, active);
        Page<TouristSpotResponseDTO> pageDTO = pageEntity.map(mapper::toResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TouristSpotResponseDTO> save(
            @RequestBody @Valid TouristSpotRequestDTO requestDTO){
        TouristSpot spot = mapper.toEntity(requestDTO);
        service.save(spot);
        TouristSpotResponseDTO responseDTO = mapper.toResponseDTO(spot);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
    ) throws IOException {
        PhotoResponseDTO response = photoService.uploadForTouristSpot(
                id, file, description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody TouristSpotUpdateDTO updateDTO){
        TouristSpot spot = service.findById(id);
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
