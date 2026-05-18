package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.TestimonialRequestDTO;
import io.github.parqueubajara.api.dto.response.TestimonialResponseDTO;
import io.github.parqueubajara.api.dto.update.TestimonialUpdateDTO;
import io.github.parqueubajara.api.mapper.TestimonialMapper;
import io.github.parqueubajara.api.model.Testimonial;
import io.github.parqueubajara.api.service.TestimonialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/testimonials")
@RequiredArgsConstructor
@Tag(name = "Testimonials", description = "Visitor testimonials and reviews")
public class TestimonialController implements GenericController {

    private final TestimonialService service;
    private final TestimonialMapper mapper;

    @GetMapping
    public ResponseEntity<Page<TestimonialResponseDTO>> getAll(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Boolean approved) {
        Page<Testimonial> page = service.findAll(pageable, approved);
        return ResponseEntity.ok(page.map(mapper::toResponseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestimonialResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponseDTO(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<TestimonialResponseDTO> save(@RequestBody @Valid TestimonialRequestDTO requestDTO) {
        Testimonial testimonial = mapper.toEntity(requestDTO);
        service.save(testimonial);
        URI location = generateHeaderLocation(testimonial.getId());
        return ResponseEntity.created(location).body(mapper.toResponseDTO(testimonial));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody TestimonialUpdateDTO updateDTO) {
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
