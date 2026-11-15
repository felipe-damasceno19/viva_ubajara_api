package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.ClaimRejectionRequestDTO;
import io.github.parqueubajara.api.dto.response.EstablishmentClaimAdminResponseDTO;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.ClaimStatus;
import io.github.parqueubajara.api.service.EstablishmentClaimService;
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
@RequestMapping("/establishment-claims")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Establishment Claims", description = "Análise de reivindicações de estabelecimentos")
public class EstablishmentClaimController {

    private final EstablishmentClaimService service;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<EstablishmentClaimAdminResponseDTO>> listAll(
            @RequestParam(required = false) ClaimStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listAll(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstablishmentClaimAdminResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<EstablishmentClaimAdminResponseDTO> approve(
            @PathVariable UUID id, Authentication authentication) {
        SystemUser admin = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(service.approve(id, admin));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<EstablishmentClaimAdminResponseDTO> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid ClaimRejectionRequestDTO dto,
            Authentication authentication) {
        SystemUser admin = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(service.reject(id, dto == null ? null : dto.reason(), admin));
    }
}
