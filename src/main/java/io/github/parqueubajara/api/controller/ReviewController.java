package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.ApprovalUpdateDTO;
import io.github.parqueubajara.api.dto.request.ReviewRequestDTO;
import io.github.parqueubajara.api.dto.response.ReviewAdminResponseDTO;
import io.github.parqueubajara.api.dto.response.ReviewResponseDTO;
import io.github.parqueubajara.api.dto.response.ReviewSummaryDTO;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.service.ReviewService;
import io.github.parqueubajara.api.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Avaliações de visitantes por estabelecimento e ponto turístico")
public class ReviewController {

    private final ReviewService service;
    private final UserService userService;

    @GetMapping("/tourist-spots/{spotId}/reviews")
    public ResponseEntity<Page<ReviewResponseDTO>> listApproved(
            @PathVariable UUID spotId,
            @PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listApproved(spotId, pageable));
    }

    @GetMapping("/tourist-spots/{spotId}/reviews/summary")
    public ResponseEntity<ReviewSummaryDTO> summary(@PathVariable UUID spotId) {
        return ResponseEntity.ok(service.summary(spotId));
    }

    @PutMapping("/reviews/spots/{spotId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDTO> save(
            @PathVariable UUID spotId,
            @RequestBody @Valid ReviewRequestDTO dto,
            Authentication authentication) {
        SystemUser user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(service.upsert(user, spotId, dto));
    }

    @GetMapping("/reviews/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDTO> mine(@RequestParam UUID spotId, Authentication authentication) {
        SystemUser user = userService.findByEmail(authentication.getName());
        return service.findMine(user, spotId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/reviews/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewAdminResponseDTO>> moderation(
            @RequestParam(required = false) ApprovalStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listForModeration(status, pageable));
    }

    @PatchMapping("/reviews/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateApprovalStatus(@PathVariable UUID id, @RequestBody @Valid ApprovalUpdateDTO dto) {
        service.updateStatus(id, dto.status());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
