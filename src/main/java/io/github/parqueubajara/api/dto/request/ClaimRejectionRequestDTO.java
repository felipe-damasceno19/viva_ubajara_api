package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.Size;

public record ClaimRejectionRequestDTO(
        @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
        String reason
) {
}
