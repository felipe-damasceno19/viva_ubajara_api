package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.TouristSpotRequestDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.response.TouristSpotResponseDTO;
import io.github.parqueubajara.api.dto.update.TouristSpotUpdateDTO;
import io.github.parqueubajara.api.mapper.TouristSpotMapper;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.service.PhotoService;
import io.github.parqueubajara.api.service.ReviewService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tourist-spots")
@RequiredArgsConstructor
@Tag(name = "Tourist Spots", description = "Gerenciamento dos pontos turísticos")
public class TouristSpotController {

    private final TouristSpotService service;
    private final TouristSpotMapper mapper;
    private final PhotoService photoService;
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<TouristSpotResponseDTO> getById(@PathVariable UUID id){
        TouristSpot spot = service.findById(id);
        return ResponseEntity.ok(withRating(mapper.toResponseDTO(spot),
                reviewService.summariesFor(List.of(spot.getId()))));
    }

    @GetMapping
    public ResponseEntity<Page<TouristSpotResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            @RequestParam(required = false) Boolean active) {
        Page<TouristSpot> pageEntity = service.findAll(pageable, active);
        Page<TouristSpotResponseDTO> pageDTO = pageEntity.map(mapper::toResponseDTO);
        Map<UUID, ReviewService.RatingSummary> summaries =
                reviewService.summariesFor(pageDTO.map(TouristSpotResponseDTO::id).getContent());
        return ResponseEntity.ok(pageDTO.map(dto -> withRating(dto, summaries)));
    }

    private TouristSpotResponseDTO withRating(TouristSpotResponseDTO dto, Map<UUID, ReviewService.RatingSummary> summaries) {
        ReviewService.RatingSummary summary = summaries.get(dto.id());
        return summary == null ? dto : dto.withRating(summary.average(), summary.count());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<TouristSpotResponseDTO> save(
            @RequestBody @Valid TouristSpotRequestDTO requestDTO){
        TouristSpot spot = mapper.toEntity(requestDTO);
        service.save(spot);
        TouristSpotResponseDTO responseDTO = mapper.toResponseDTO(spot);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody TouristSpotUpdateDTO updateDTO){
        TouristSpot spot = service.findById(id);
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
