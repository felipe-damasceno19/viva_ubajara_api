package io.github.parqueubajara.api.dto.response;

import java.util.List;
import java.util.UUID;

public record AirportResponseDTO(
        UUID id,
        String iataCode,
        String city,
        Double distanceKm,
        Integer estimatedTimeMinutes,
        String routeDescription,
        List<PhotoResponseDTO> photos
) {
}
