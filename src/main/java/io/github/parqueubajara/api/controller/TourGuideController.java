package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.TourGuideRequestDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.response.TourGuideResponseDTO;
import io.github.parqueubajara.api.dto.update.TourGuideUpdateDTO;
import io.github.parqueubajara.api.mapper.TourGuideMapper;
import io.github.parqueubajara.api.model.TourGuide;
import io.github.parqueubajara.api.service.PhotoService;
import io.github.parqueubajara.api.service.TourGuideService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/tour-guides")
@RequiredArgsConstructor
@Tag(name = "Tour Guides", description = "Gerenciamento dos guias turísticos")
public class TourGuideController implements GenericController {

    private final TourGuideService service;
    private final TourGuideMapper mapper;
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<TourGuideResponseDTO> getById(@PathVariable UUID id){
        TourGuide guide = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(guide));
    }

    @GetMapping
    public ResponseEntity<Page<TourGuideResponseDTO>> getAll(@PageableDefault(size = 10)Pageable pageable){
        Page<TourGuide> pageEntity = service.findAll(pageable);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @PostMapping
    public ResponseEntity<TourGuideResponseDTO> save(@RequestBody @Valid TourGuideRequestDTO requestDTO){
        TourGuide guide = mapper.toEntity(requestDTO);
        service.save(guide);
        URI location = generateHeaderLocation(guide.getId());

        return ResponseEntity.created(location).body(mapper.toResponseDTO(guide));
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
    ) throws IOException {
        PhotoResponseDTO response = photoService.uploadForTourGuide(
                id, file, description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody TourGuideUpdateDTO updateDTO){
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
