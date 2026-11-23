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
import io.github.parqueubajara.api.service.ReviewService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/attractions")
@RequiredArgsConstructor
@Tag(name = "Attractions", description = "Gerenciamento dos atrativos turísticos")
public class AttractionController implements GenericController{

    private final AttractionService service;
    private final AttractionMapper mapper;
    private final PhotoService photoService;
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<AttractionResponseDTO> getById(@PathVariable UUID id){
        Attraction attraction = service.findById(id);
        return ResponseEntity.ok(withRating(mapper.toResponseDTO(attraction),
                reviewService.summariesFor(List.of(attraction.getId()))));
    }

    @GetMapping()
    public ResponseEntity<Page<AttractionResponseDTO>> getAll(
            @RequestParam(required = false) AttractionType category,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "name") Pageable pageable){

        Page<Attraction> pageEntity = service.findAll(pageable, category, active);
        return ResponseEntity.ok(withRatings(pageEntity.map(mapper::toResponseDTO)));
    }

    private Page<AttractionResponseDTO> withRatings(Page<AttractionResponseDTO> page) {
        Map<UUID, ReviewService.RatingSummary> summaries =
                reviewService.summariesFor(page.map(AttractionResponseDTO::id).getContent());
        return page.map(dto -> withRating(dto, summaries));
    }

    private AttractionResponseDTO withRating(AttractionResponseDTO dto, Map<UUID, ReviewService.RatingSummary> summaries) {
        ReviewService.RatingSummary summary = summaries.get(dto.id());
        return summary == null ? dto : dto.withRating(summary.average(), summary.count());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<AttractionResponseDTO> save(@RequestBody @Valid AttractionRequestDTO requestDTO){
        Attraction attraction = mapper.toEntity(requestDTO);
        Attraction saved = service.save(attraction, requestDTO.linkedSpotIds());
        URI location = generateHeaderLocation(saved.getId());
        return ResponseEntity.created(location).body(mapper.toResponseDTO(saved));
    }

    @PostMapping("/{id}/sub-attractions")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<AttractionResponseDTO> addSubAttraction(
            @PathVariable UUID id,
            @RequestBody AttractionRequestDTO requestDTO) {
        Attraction child = mapper.toEntity(requestDTO);
        Attraction saved = service.addSubAttraction(id, child);
        URI location = generateHeaderLocation(saved.getId());
        return ResponseEntity.created(location).body(mapper.toResponseDTO(saved));
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody AttractionUpdateDTO updateDTO){
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
