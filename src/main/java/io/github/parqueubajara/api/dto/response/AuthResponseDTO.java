package io.github.parqueubajara.api.dto.response;

public record AuthResponseDTO(
        String token,
        String email,
        String role,
        String name,
        String photo
) {
}
