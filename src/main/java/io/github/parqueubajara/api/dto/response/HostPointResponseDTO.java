package io.github.parqueubajara.api.dto.response;

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
        HostType hostType,
        Integer numOfRooms,
        String avgPrice,
        String bookingUrl,
        List<PhotoResponseDTO> photos
) {
}
