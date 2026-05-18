package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.ContactMessageRequestDTO;
import io.github.parqueubajara.api.dto.response.ContactMessageResponseDTO;
import io.github.parqueubajara.api.mapper.ContactMessageMapper;
import io.github.parqueubajara.api.model.ContactMessage;
import io.github.parqueubajara.api.service.ContactMessageService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/contact-messages")
@RequiredArgsConstructor
@Tag(name = "Contact Messages", description = "Visitor contact form submissions")
public class ContactMessageController implements GenericController {

    private final ContactMessageService service;
    private final ContactMessageMapper mapper;

    @PostMapping
    public ResponseEntity<ContactMessageResponseDTO> save(@RequestBody @Valid ContactMessageRequestDTO requestDTO) {
        ContactMessage message = mapper.toEntity(requestDTO);
        service.save(message);
        URI location = generateHeaderLocation(message.getId());
        return ResponseEntity.created(location).body(mapper.toResponseDTO(message));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ContactMessageResponseDTO>> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Boolean read) {
        Page<ContactMessage> page = service.findAll(pageable, read);
        return ResponseEntity.ok(page.map(mapper::toResponseDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponseDTO(service.findById(id)));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countUnread() {
        return ResponseEntity.ok(Map.of("count", service.countUnread()));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        service.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
