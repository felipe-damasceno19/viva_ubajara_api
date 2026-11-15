package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDTO(
        UUID id,
        int rating,
        String comment,
        String authorName,
        String authorPhoto,
        ApprovalStatus status,
        LocalDateTime createdDate
) {
}
