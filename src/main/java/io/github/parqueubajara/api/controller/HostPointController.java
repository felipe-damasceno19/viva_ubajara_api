package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.HostPointRequestDTO;
import io.github.parqueubajara.api.dto.response.HostPointResponseDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.update.HostPointUpdateDTO;
import io.github.parqueubajara.api.mapper.HostPointMapper;
import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.enums.HostType;
import io.github.parqueubajara.api.service.HostPointService;
import io.github.parqueubajara.api.service.PhotoService;
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
@RequestMapping("/host-points")
@RequiredArgsConstructor
@Tag(name = "Host Points", description = "Gerenciamento das hospedagens")
public class HostPointController implements GenericController {

    private final HostPointService service;
    private final HostPointMapper mapper;
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<HostPointResponseDTO> getById(@PathVariable UUID id){
        HostPoint hostPoint = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(hostPoint));
    }

    @GetMapping
    public ResponseEntity<Page<HostPointResponseDTO>> getAll(@PageableDefault(size = 10)Pageable pageable,
                                                             @RequestParam(required = false)HostType type) {
        Page<HostPoint> pageEntity = service.findAll(pageable, type);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @PostMapping
    public ResponseEntity<HostPointResponseDTO> save(@RequestBody @Valid HostPointRequestDTO requestDTO){
        HostPoint hostPoint = mapper.toEntity(requestDTO);
        service.save(hostPoint);
        URI location = generateHeaderLocation(hostPoint.getId());

        return ResponseEntity.created(location).body(mapper.toResponseDTO(hostPoint));
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
    ) throws IOException {
        PhotoResponseDTO response = photoService.uploadForHostPoint(
                id, file, description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody HostPointUpdateDTO updateDTO){
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
