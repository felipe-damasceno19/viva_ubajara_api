package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.model.enums.HostType;

import java.util.List;
import java.util.UUID;

public record HostPointResponseDTO(
        UUID id,
        String name,
        String description,
        String address,
        String phone,
        String email,
        String webUrl,
        String instagramUrl,
        String mapsUrl,
        Boolean active,
        ApprovalStatus approvalStatus,
        boolean claimable,
        HostType hostType,
        Integer numOfRooms,
        String avgPrice,
        String bookingUrl,
        List<PhotoResponseDTO> photos,
        Double averageRating,
        long reviewCount
) {

    public HostPointResponseDTO withRating(Double averageRating, long reviewCount) {
        return new HostPointResponseDTO(
                id, name, description, address, phone, email, webUrl, instagramUrl, mapsUrl, active,
                approvalStatus, claimable, hostType, numOfRooms, avgPrice, bookingUrl, photos,
                averageRating, reviewCount);
    }
}
