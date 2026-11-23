package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewAdminResponseDTO(
        UUID id,
        int rating,
        String comment,
        ApprovalStatus status,
        LocalDateTime createdDate,
        Spot spot,
        Author author
) {

    public record Spot(UUID id, String name, String type) {
    }

    public record Author(UUID id, String name, String email) {
    }
}
