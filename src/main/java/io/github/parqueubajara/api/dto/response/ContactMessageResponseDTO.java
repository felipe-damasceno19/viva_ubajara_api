package io.github.parqueubajara.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContactMessageResponseDTO(

        UUID id,
        String name,
        String email,
        String subject,
        String message,
        Boolean read,
        LocalDateTime createdDate

) {
}
