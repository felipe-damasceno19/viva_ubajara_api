package io.github.parqueubajara.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PhotoResponseDTO(
        UUID id,
        String url,
        String description,
        Integer displayOrder,
        String ownerType,
        UUID ownerId,
        LocalDateTime createdAt
) {
}
