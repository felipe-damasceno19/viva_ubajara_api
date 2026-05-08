package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.AirportRequestDTO;
import io.github.parqueubajara.api.dto.response.AirportResponseDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.update.AirportUpdateDTO;
import io.github.parqueubajara.api.mapper.AirportMapper;
import io.github.parqueubajara.api.model.Airport;
import io.github.parqueubajara.api.service.AirportService;
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
@RequestMapping("/airports")
@RequiredArgsConstructor
@Tag(name = "Airports", description = "Aeroportos próximos a Ubajara")
public class AirportController implements GenericController {

    private final AirportService service;
    private final AirportMapper mapper;
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<AirportResponseDTO> getById(@PathVariable UUID id){
        Airport airport = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(airport));
    }

    @GetMapping
    public ResponseEntity<Page<AirportResponseDTO>> getAll(@PageableDefault(size = 10)Pageable pageable){
        Page<Airport> pageEntity = service.findAll(pageable);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AirportResponseDTO> save(@RequestBody @Valid AirportRequestDTO requestDTO){
        Airport airport = mapper.toEntity(requestDTO);
        service.save(airport);
        URI location = generateHeaderLocation(airport.getId());

        return ResponseEntity.created(location).body(mapper.toResponseDTO(airport));
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
    ) throws IOException {
        PhotoResponseDTO response = photoService.uploadForAirport(
                id, file, description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> udpdate(@PathVariable UUID id, @RequestBody AirportUpdateDTO updateDTO){
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
