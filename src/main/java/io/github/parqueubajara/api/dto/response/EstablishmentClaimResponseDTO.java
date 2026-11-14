package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.ClaimStatus;
import io.github.parqueubajara.api.model.enums.EstablishmentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record EstablishmentClaimResponseDTO(
        UUID id,
        UUID spotId,
        String spotName,
        EstablishmentType spotType,
        ClaimStatus status,
        String message,
        String contactPhone,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
}
