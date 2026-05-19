package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.Role;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String username,
        String email,
        Role role,
        String photoUrl
) {
}
