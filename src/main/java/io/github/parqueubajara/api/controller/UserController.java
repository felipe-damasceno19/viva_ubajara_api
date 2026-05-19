package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.UserRequestDTO;
import io.github.parqueubajara.api.dto.response.UserResponseDTO;
import io.github.parqueubajara.api.dto.update.UserProfileUpdateDTO;
import io.github.parqueubajara.api.dto.update.UserUpdateDTO;
import io.github.parqueubajara.api.mapper.UserMapper;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gerenciamento de usuários")
public class UserController implements GenericController{

    private final UserService service;
    private final UserMapper mapper;


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        SystemUser user = service.findByEmail(email);
        return ResponseEntity.ok(mapper.toResponseDTO(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateMe(@RequestBody @Valid UserProfileUpdateDTO updateDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        service.updateSelf(email, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String photoUrl = service.uploadPhoto(email, file);
        return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> findAll(@PageableDefault(size = 10) Pageable pageable,
                                                         @RequestParam(required = false) String username){
        Page<SystemUser> pageEntity = service.findAll(pageable, username);
        return ResponseEntity.ok(pageEntity.map(mapper::toResponseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable UUID id){
        SystemUser user = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(user));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody @Valid UserUpdateDTO updateDTO){
        service.update(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
