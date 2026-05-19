package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.EventRequestDTO;
import io.github.parqueubajara.api.dto.response.EventResponseDTO;
import io.github.parqueubajara.api.dto.response.PhotoResponseDTO;
import io.github.parqueubajara.api.dto.update.EventUpdateDTO;
import io.github.parqueubajara.api.mapper.EventMapper;
import io.github.parqueubajara.api.model.Event;
import io.github.parqueubajara.api.service.EventService;
import io.github.parqueubajara.api.service.PhotoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Gerenciamento dos eventos")
public class EventController implements GenericController{

    private final EventService service;
    private final EventMapper mapper;
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getById(@PathVariable UUID id){
        Event event = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(event));
    }

    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAll(@PageableDefault(size = 10) Pageable pageable,
                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime startDateTime,
                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime endDateTime,
                                                         @RequestParam(required = false) Boolean active){
        Page<Event> pageEntity = service.findAll(pageable, startDateTime, endDateTime, active);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<EventResponseDTO> save(@RequestBody @Valid EventRequestDTO requestDTO){
        Event event = mapper.toEntity(requestDTO);
        service.save(event);
        URI location = generateHeaderLocation(event.getId());

        return ResponseEntity.created(location).body(mapper.toResponseDTO(event));
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "displayOrder", required = false) String displayOrder
    ) throws IOException {
        PhotoResponseDTO response = photoService.uploadForEvent(
                id, file, description,
                displayOrder != null ? Integer.parseInt(displayOrder) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody EventUpdateDTO updateDTO){
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
