package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.CompanyEstablishmentRequestDTO;
import io.github.parqueubajara.api.dto.request.GastronomyItemRequestDTO;
import io.github.parqueubajara.api.dto.response.CompanyEstablishmentResponseDTO;
import io.github.parqueubajara.api.dto.response.GastronomyItemResponseDTO;
import io.github.parqueubajara.api.dto.update.CompanyEstablishmentUpdateDTO;
import io.github.parqueubajara.api.dto.update.GastronomyItemUpdateDTO;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.service.CompanyService;
import io.github.parqueubajara.api.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPRESA')")
@Tag(name = "Companies", description = "Área self-service da empresa")
public class CompanyController {

    private final CompanyService service;
    private final UserService userService;

    @GetMapping("/establishment")
    public ResponseEntity<CompanyEstablishmentResponseDTO> getEstablishment() {
        SystemUser user = getAuthenticatedUser();
        return ResponseEntity.ok(service.getMyEstablishment(user));
    }

    @PostMapping("/establishment")
    public ResponseEntity<CompanyEstablishmentResponseDTO> createEstablishment(@RequestBody @Valid CompanyEstablishmentRequestDTO dto) {
        SystemUser user = getAuthenticatedUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEstablishment(user, dto));
    }

    @PutMapping("/establishment")
    public ResponseEntity<CompanyEstablishmentResponseDTO> updateEstablishment(@RequestBody CompanyEstablishmentUpdateDTO dto) {
        SystemUser user = getAuthenticatedUser();
        return ResponseEntity.ok(service.updateEstablishment(user, dto));
    }

    @GetMapping("/menu-items")
    public ResponseEntity<List<GastronomyItemResponseDTO>> listMenuItems() {
        SystemUser user = getAuthenticatedUser();
        return ResponseEntity.ok(service.listMenuItems(user));
    }

    @PostMapping("/menu-items")
    public ResponseEntity<GastronomyItemResponseDTO> createMenuItem(@RequestBody @Valid GastronomyItemRequestDTO dto) {
        SystemUser user = getAuthenticatedUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMenuItem(user, dto));
    }

    @PutMapping("/menu-items/{id}")
    public ResponseEntity<Void> updateMenuItem(@PathVariable UUID id, @RequestBody GastronomyItemUpdateDTO dto) {
        SystemUser user = getAuthenticatedUser();
        service.updateMenuItem(user, id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/menu-items/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable UUID id) {
        SystemUser user = getAuthenticatedUser();
        service.deleteMenuItem(user, id);
        return ResponseEntity.noContent().build();
    }

    private SystemUser getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email);
    }
}
