package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.model.enums.ClaimStatus;
import io.github.parqueubajara.api.model.enums.EstablishmentType;
import io.github.parqueubajara.api.model.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record EstablishmentClaimAdminResponseDTO(
        UUID id,
        UUID spotId,
        String spotName,
        EstablishmentType spotType,
        ClaimStatus status,
        String message,
        String contactPhone,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        String rejectionReason,
        Claimant claimant,
        Spot spot,
        String reviewedByName
) {

    public record Claimant(
            UUID id,
            String name,
            String email,
            String companyName,
            String cnpj,
            Role role
    ) {
    }

    public record Spot(
            UUID id,
            String name,
            EstablishmentType type,
            String address,
            Boolean active,
            ApprovalStatus approvalStatus,
            boolean hasOwner
    ) {
    }
}
