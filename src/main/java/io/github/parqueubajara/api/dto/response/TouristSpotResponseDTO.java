package io.github.parqueubajara.api.dto.response;

import java.util.List;
import java.util.UUID;

public record TouristSpotResponseDTO(
        UUID id,
        String name,
        String description,
        String address,
        String phone,
        String email,
        String webUrl,
        String instagramUrl,
        Boolean active,
        List<PhotoResponseDTO> photos
) {
}
