package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;

import java.util.List;
import java.util.UUID;

public record RestaurantResponseDTO(
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
        String cuisineType,
        String openingHours,
        String avgPrice,
        Boolean acceptsReservation,
        Integer starRating,
        List<PhotoResponseDTO> photos,
        Double averageRating,
        long reviewCount
) {

    public RestaurantResponseDTO withRating(Double averageRating, long reviewCount) {
        return new RestaurantResponseDTO(
                id, name, description, address, phone, email, webUrl, instagramUrl, mapsUrl, active,
                approvalStatus, claimable, cuisineType, openingHours, avgPrice, acceptsReservation,
                starRating, photos, averageRating, reviewCount);
    }
}
