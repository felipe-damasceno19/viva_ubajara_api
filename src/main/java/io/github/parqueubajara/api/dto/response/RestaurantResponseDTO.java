package io.github.parqueubajara.api.dto.response;

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
        String cuisineType,
        String openingHours,
        String avgPrice,
        Boolean acceptsReservation,
        Integer starRating,
        List<PhotoResponseDTO> photos
) {
}
